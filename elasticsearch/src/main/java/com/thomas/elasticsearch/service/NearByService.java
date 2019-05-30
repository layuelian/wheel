package com.thomas.elasticsearch.service;

import com.thomas.elasticsearch.model.People;
import com.thomas.elasticsearch.model.SearchResult;
import com.thomas.elasticsearch.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jws.Oneway;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: lf
 * @Date: 2019/5/24 10:16
 */
@Service
@Slf4j
public class NearByService {
    @Autowired
    private TransportClient client;

    private String indexName = "nearby";//相当于数据库名称
    private String indexType = "wechat";//相当于表名


    /**
     * 建库建表建约束
     */
    public void recreateIndex() {
        try {
            //后台级操作，关乎到删库跑路的危险
            if (!client.admin().indices().prepareExists(indexName).execute().get().isExists()) {
                return;
            }
            //先清除原来已有的数据库
            client.admin().indices().prepareDelete(indexName).execute().actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        createIndex();
    }

    /**
     * 创建索引
     */
    private void createIndex() {
        //表结构（建约束）
        XContentBuilder mapping = createMapping();
        //建库
        //建库建表建约束
        CreateIndexResponse createIndexResponse = client.admin().indices().prepareCreate(indexName).execute().actionGet();
        if (!createIndexResponse.isAcknowledged()) {
            log.info("无法创建索引[" + indexName + "]");
        }
        //建表
        PutMappingRequest putMapping = Requests.putMappingRequest(indexName).type(indexType).source(mapping);
        AcknowledgedResponse response = client.admin().indices().putMapping(putMapping).actionGet();

        if (!response.isAcknowledged()) {
            log.info("无法创建[" + indexName + "] [" + indexType + "]的Mapping");
        } else {
            log.info("创建[" + indexName + "] [" + indexType + "]的Mapping成功");
        }
    }

    /**
     * 创建mapping，相当于创建表结构
     *
     * @return
     */
    private XContentBuilder createMapping() {
        XContentBuilder mapping = null;
        try {
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    //索引库名（类似数据库中的表）
                    .startObject(indexType)
                    .startObject("properties")
                    //微信号(唯一的索引)
                    .startObject("wxNo").field("type", "keyword").endObject()
                    .startObject("nickName").field("type", "keyword").endObject()
                    .startObject("sex").field("type", "keyword").endObject()
                    .startObject("location").field("type", "geo_point").endObject()
                    .endObject()
                    .endObject()
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapping;
    }

    /**
     * 准备造假数据，这些数值会随机生成
     *
     * @param myLat 纬度
     * @param myLon 经度
     * @param count 生成多少个
     * @return
     */
    public Integer addDataToIndex(double myLat, double myLon, int count) {
        List<XContentBuilder> contents = new ArrayList<XContentBuilder>();
        //开启重复校验的缓存区
        RandomUtil.openCache();
        for (long i = 0; i < count; i++) {
            People people = randomPeople(myLat, myLon);
            contents.add(obj2XContent(people));
        }
        //清空重复校验的缓存区
        RandomUtil.clearCache();

        BulkRequestBuilder bulkRequestBuilder=client.prepareBulk();
        for (XContentBuilder content : contents) {
            IndexRequest request = client.prepareIndex(indexName, indexType).setSource(content).request();
            bulkRequestBuilder.add(request);
        }
        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
        if(bulkResponse.hasFailures()){
            log.info("创建索引出错!");
        }
        return bulkRequestBuilder.numberOfActions();
    }

    public SearchResult search(double lat, double lon, int radius, int size, String sex){
        SearchResult result=new SearchResult();

        //同一单位为米
        String unit= DistanceUnit.METERS.toString();

        //获取一个查询规则构造器
        //查哪个库哪个表
        //完成了相当于 select * from 库名.表名
        SearchRequestBuilder srb = client.prepareSearch(indexName).setTypes(indexType);

        //实现分页操作
        //相当于Mysql中的 limit 0，size
        srb.setFrom(0).setSize(size);//取出优先级最高的size条数据

        //拼接查询条件
        //性别、昵称、坐标
        //地理坐标，方圆多少米以内都要找出来
        QueryBuilder qb = QueryBuilders.geoDistanceQuery("location")
                .point(lat, lon)
                .distance(radius, DistanceUnit.METERS)
                .geoDistance(GeoDistance.PLANE);//设置计算规则,平面还是立方
        //相当于where location>0 and location<radius
        srb.setPostFilter(qb);

        //继续拼接where条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if(!(sex==null||"sex".equals(sex.trim()))){
            boolQueryBuilder.must(QueryBuilders.matchQuery("sex",sex));
        }

        srb.setQuery(boolQueryBuilder);
        //...

        //设置排序规则

        GeoDistanceSortBuilder geoSort = SortBuilders.geoDistanceSort("location", lat, lon);
        geoSort.unit(DistanceUnit.METERS);
        geoSort.order(SortOrder.ASC);//按距离升序排序，最近的要排在最前面
        //order by location asc
        srb.addSort(geoSort);

        //到此为止，就想到于Sql语句构建完毕
        //调用 execute()方法
        //Response
        SearchResponse response = srb.execute().actionGet();

        //高亮分词
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();

        //搜索耗时
        Float usetime = response.getTook().getMillis() / 1000f;

        result.setUseTime(usetime);
        result.setTotal(hits.getTotalHits());
        result.setDistance(DistanceUnit.METERS.toString());
        result.setData(new ArrayList<Map<String,Object>>());
        for (SearchHit hit : searchHits) {
            //获取距离值
            BigDecimal geoDis=new BigDecimal((Double) hit.getSortValues()[0]);
            Map<String,Object> hitMap=hit.getSourceAsMap();
            hitMap.put("geoDistance",geoDis.setScale(0, BigDecimal.ROUND_HALF_DOWN));
            result.getData().add(hitMap);
        }
        return result;
    }

    /**
     * 讲java对象转为JSON字符串（所谓的全文检索，玩的就是字符串）
     *
     * @param people
     * @return
     */
    private XContentBuilder obj2XContent(People people) {
        XContentBuilder jsonBuild = null;
        try {
            jsonBuild = XContentFactory.jsonBuilder();
            jsonBuild.startObject()
                    .field("wxNo",people.getWxNo())
                    .field("nickName",people.getNickName())
                    .field("sex",people.getSex())
                    .startObject("location")
                    .field("lat",people.getLat())
                    .field("lon",people.getLon())
                    .endObject()
                    .endObject();
            log.info(Strings.toString(jsonBuild));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonBuild;
    }

    public People randomPeople(double myLat, double myLon) {
        //随机生成微信号
        String wxNo = RandomUtil.randomWxNo();
        //造人计划，性别随机
        String sex = RandomUtil.randomSex();
        //生成随机昵称
        String nickName = RandomUtil.randomNickName(sex);
        //生成随机坐标
        double[] point = RandomUtil.randomPoint(myLat, myLon);
        return new People(point[0], point[1], wxNo, nickName, sex);
    }
}
