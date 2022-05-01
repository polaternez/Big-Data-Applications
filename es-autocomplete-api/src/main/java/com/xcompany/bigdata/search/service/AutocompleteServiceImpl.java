package com.xcompany.bigdata.search.service;

import com.google.gson.Gson;
import com.xcompany.bigdata.search.model.AutocompleteDetail;
import com.xcompany.bigdata.search.model.AutocompleteResponse;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.util.ArrayList;

import static com.xcompany.bigdata.search.model.Constants.*;

@Service
public class AutocompleteServiceImpl implements AutocompleteService{

    RestHighLevelClient client;
    SearchRequest request;
    SearchSourceBuilder sourceBuilder;

    Gson gson;

    @PostConstruct
    public void init(){
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(ES_HOSTNAME, ES_PORT)));
        request = new SearchRequest(ES_INDEX);
        sourceBuilder = new SearchSourceBuilder();

        gson = new Gson();
    }

    @Override
    public AutocompleteResponse search(String term) throws IOException {
        ArrayList<AutocompleteDetail> data = new ArrayList<>();

        sourceBuilder.from(0);
        sourceBuilder.size(3);
        sourceBuilder.query(QueryBuilders.matchQuery(ES_AUTOCOMPLETE_FIELD, term).fuzziness(2));
        request.source(sourceBuilder);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        SearchHit[] hitsDetail = hits.getHits();

        for (int i=0; i<hitsDetail.length; i++){
            String responseDetail = hitsDetail[i].getSourceAsString();
            AutocompleteDetail detail = gson.fromJson(responseDetail, AutocompleteDetail.class);
            data.add(detail);
        }

        return new AutocompleteResponse(data);
    }
}
