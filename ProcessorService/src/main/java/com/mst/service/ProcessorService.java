package com.mst.service;

import com.mst.client.LoaderClient;
import com.mst.client.MetricClient;
import com.mst.model.Loader;
import com.mst.model.Metric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessorService {
    @Autowired
    private LoaderClient loaderClient;

    @Autowired
    private MetricClient metricClient;
    private void test(String info) {

        ACTION action = new Action(info);
        ResponseEntity<List<Loader>> resLoader = loaderClient.getAllData();
        List<Loader> dataLoader =  resLoader.getBody();

        ResponseEntity<List<Metric>> resMetric = metricClient.getAllData();
        List<Metric> dataMetric =  resMetric.getBody();


        //get data actions from kafka
        //actionkafka -> conition = [[1]]
        //dataMetric -> Metric id 1
        //int count =dataLoader -> get count(metric1.label) => 9
        //boolean b = count >= metric1.threshold
        //if b == true -> if(metric1.action.type=="EMAIL")->
        //kafkaTemplate.send("emailTopic", message);
    }

    @KafkaListener(topics="actionTopic", groupId="MST")
    public void listen(String info) {
        test(info);
    }

}
