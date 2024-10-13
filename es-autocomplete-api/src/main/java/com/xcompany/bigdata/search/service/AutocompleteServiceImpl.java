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
    RestHighLevelClient esClient;
    SearchRequest request;
    SearchSourceBuilder sourceBuilder;
    Gson gson;

    @PostConstruct
    public void init(){
        esClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(ES_HOSTNAME, ES_PORT))
        );
        request = new SearchRequest(ES_INDEX);
        sourceBuilder = new SearchSourceBuilder();
        gson = new Gson();
    }

    @Override
    public AutocompleteResponse search(String term) throws IOException {
        ArrayList<AutocompleteDetail> acList = new ArrayList<>();

        sourceBuilder.from(0);
        sourceBuilder.size(3);
        sourceBuilder.query(
                QueryBuilders.matchQuery(ES_AUTOCOMPLETE_FIELD, term).fuzziness(2)
        );
        request.source(sourceBuilder);

        SearchResponse searchResponse = esClient.search(request, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for(SearchHit hit : hits){
            String hitDetail = hit.getSourceAsString();
            AutocompleteDetail acDetail = gson.fromJson(hitDetail, AutocompleteDetail.class);
            acList.add(acDetail);
        }

        return new AutocompleteResponse(acList);
    }
}
