package com.lenovo.msa.account;

/**
 * FileName: BaseTest
 * Author:   lujy7
 * Date:     2019/6/17 16:20
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */

//import com.lenovo.msa.account.model.LoyaltyCustomerProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResendApplication.class)
public class BaseTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testBusiness() {
        /*List<LoyaltyCustomerProfile> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++ ) {
            LoyaltyCustomerProfile customerProfile = new LoyaltyCustomerProfile();
            customerProfile.setUserId("test@lenovo.com");
            customerProfile.setUserStatus("pending");
            customerProfile.setFirstName("firstName-"+i);
            customerProfile.setLastName("lastName-"+i);
            customerProfile.setCountry("US");
            customerProfile.setStore("store");
            list.add(customerProfile);
        }
        long begin = System.currentTimeMillis();
        for (LoyaltyCustomerProfile loyaltyCustomerProfile : list) {
            Update update = new Update();
            update.set("userId", loyaltyCustomerProfile.getUserId());
            update.set("userStatus", loyaltyCustomerProfile.getUserStatus());
            update.set("firstName", loyaltyCustomerProfile.getFirstName());
            update.set("lastName", loyaltyCustomerProfile.getLastName());
            update.set("country", loyaltyCustomerProfile.getCountry());
            update.set("store", loyaltyCustomerProfile.getStore());
            mongoTemplate.upsert(new Query(Criteria.where("userId").is(loyaltyCustomerProfile.getUserId())),
                    update, LoyaltyCustomerProfile.class);
        }
        log.info("time : {}" , System.currentTimeMillis()-begin);*/
    }

}
