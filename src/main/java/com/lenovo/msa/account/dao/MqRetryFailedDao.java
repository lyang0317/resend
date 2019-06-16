package com.lenovo.msa.account.dao;

//import com.lenovo.liecomm.microservices.common.mongodb.MongoHelper;
import com.lenovo.msa.account.model.MqRetryFailed;
import com.mongodb.WriteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MqRetryFailedDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Integer createRecord(MqRetryFailed data) {
        Update update = new Update();
        update.set("uniformId", data.getUniformId());
        update.set("type", data.getType());
        update.set("request", data.getRequest());
        update.set("mq", data.getMq());
        update.set("stackTrace", data.getStackTrace());
        update.set("timestamp", data.getTimestamp());
//        WriteResult result = MongoHelper.mongoTemplate.upsert(new Query(Criteria.where("uniformId").is(data.getUniformId())),
//                update, MqRetryFailed.class);
        WriteResult result = mongoTemplate.upsert(new Query(Criteria.where("uniformId").is(data.getUniformId())),
                update, MqRetryFailed.class);
        return result == null ? 0 : result.getN();
    }
}
