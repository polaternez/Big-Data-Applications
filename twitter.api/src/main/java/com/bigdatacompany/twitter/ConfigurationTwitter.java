package com.bigdatacompany.twitter;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.ArrayList;

public class ConfigurationTwitter {
    //For authentication
    public static ConfigurationBuilder getConfig(){
        ConfigurationBuilder cb = new ConfigurationBuilder();

        cb.setOAuthConsumerKey(Constants.OAuthConsumerKey);
        cb.setOAuthConsumerSecret(Constants.OAuthConsumerSecret);
        cb.setOAuthAccessToken(Constants.OAuthAccessToken);
        cb.setOAuthAccessTokenSecret(Constants.OAuthAccessTokenSecret);
        return  cb;
    }
    // Algorithm for pass Twitter API tweet limit (=100 tweets)
    public static ArrayList<Status>  getAdvancedSearch(Twitter twitter){
        Query query = new Query("Türkiye");
        int numberOfTweets = 5000;
        long lastID = Long.MAX_VALUE;
        ArrayList<Status> tweets = new ArrayList<>();

        while (tweets.size () < numberOfTweets) {
            if (numberOfTweets - tweets.size() > 100)
                query.setCount(100);
            else
                query.setCount(numberOfTweets - tweets.size());

            try {
                QueryResult result = twitter.search(query);
                tweets.addAll(result.getTweets());
                System.out.println("Gathered " + tweets.size() + " tweets\n");

                for (Status t : tweets)
                    if(t.getId() < lastID)
                        lastID = t.getId();

            }
            catch (TwitterException te) {
                System.out.println("Couldn't connect: " + te);
            }
            query.setMaxId(lastID - 1);
        }
        return  tweets;
    }
}
