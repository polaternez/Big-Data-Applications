package com.bigdatacompany.twitter;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.lang.module.Configuration;
import java.util.List;

public class Application {
    public static void main(String[] args) throws TwitterException {

        MongoClient mongoClient = new MongoClient(Constants.MONGO_HOST, Constants.MONGO_PORT);
        MongoDatabase twitterDB = mongoClient.getDatabase("twitterDB");
        MongoCollection<Document> searchCollection = twitterDB.getCollection("search");

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("xxx");
        cb.setOAuthConsumerSecret("xxx");
        cb.setOAuthAccessToken("xxx");
        cb.setOAuthAccessTokenSecret("xxx");

        TwitterFactory tf = new TwitterFactory(cb.build());

        Twitter twitter = tf.getInstance();

       /* List<Status> responseList = twitter.getUserTimeline("elonmusk", new Paging(3,200));

        for (Status st : responseList){
            System.out.println("Time: " +st.getCreatedAt()+"Tweet: "+st.getText()+"Fav: "+st.getFavoriteCount()+"Retweet: "+st.getRetweetCount());
        }*/
// Upload tweets to MongoDB
        Query query = new Query();
        query.setQuery("Büyük Veri");
        query.setCount(100);

        QueryResult search = twitter.search(query);

        List<Status> responseList = search.getTweets();

        for (Status st : responseList){
//            System.out.println("Time: " + st.getCreatedAt() + "Tweet: " + st.getText()+"User: " + st.getUser().getScreenName());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("screen_name", st.getUser().getScreenName());
            jsonObject.put("tweet", st.getText());
            jsonObject.put("create_date", st.getCreatedAt());
            jsonObject.put("followers_count", st.getUser().getFollowersCount());
            jsonObject.put("friend_count", st.getUser().getFriendsCount());
            jsonObject.put("description", st.getUser().getDescription());
            jsonObject.put("fav_count", st.getFavoriteCount());
            jsonObject.put("retweet_count", st.getRetweetCount());
            String email = st.getUser().getEmail();
            if (email != null)
                jsonObject.put("email", email);

            searchCollection.insertOne(Document.parse(jsonObject.toString()));

        }


    }
}
