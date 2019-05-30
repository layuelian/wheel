package com.thomas.elasticsearch.service;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @Author: lf
 * @Date: 2019/5/24 9:38
 */
@Service
@Slf4j
public class BookService {
    private String indexName = "book";//相当于数据库名称
    private String indexType = "novel";//相当于表名
    @Autowired
    private TransportClient client;

    //根据id获取对象
    public GetResponse getById(String id) {
        return this.client.prepareGet(indexName, indexType, id).get();
    }
    //增
    public IndexResponse add(String title, String author, int wordCount, String publishDate) throws IOException {
        XContentBuilder context = XContentFactory.jsonBuilder().startObject()
                .field("title", title)
                .field("author", author)
                .field("publish_date", publishDate)
                .field("word_count", wordCount)
                .endObject();
        IndexResponse response = this.client.prepareIndex().setSource(context).get();
        return response;
    }
    //删
    public DeleteResponse remove(String id){
        return this.client.prepareDelete(indexName,indexType,id).get();
    }
    //改
    public UpdateResponse modify(String id,String title,String author) throws Exception {
        UpdateRequest updateRequest=new UpdateRequest(indexName,indexType,id);
        XContentBuilder builder=XContentFactory.jsonBuilder().startObject();
        if(title!=null){
            builder.field("title",title);
        }
        if(author!=null){
            builder.field("author",author);
        }
        builder.endObject();
        updateRequest.doc(builder);
        return this.client.update(updateRequest).get();
    }
}
