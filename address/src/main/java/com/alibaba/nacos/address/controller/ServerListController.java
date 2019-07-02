/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.address.controller;

import com.alibaba.nacos.address.component.AddressServerGeneratorManager;
import com.alibaba.nacos.address.component.AddressServerManager;
import com.alibaba.nacos.address.constant.AddressServerConstants;
import com.alibaba.nacos.naming.core.Cluster;
import com.alibaba.nacos.naming.core.Service;
import com.alibaba.nacos.naming.core.ServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pbting
 * @date 2019-06-18 5:04 PM
 */
@RestController
public class ServerListController {

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private AddressServerManager addressServerManager;

    @Autowired
    private AddressServerGeneratorManager addressServerBuilderManager;

    /**
     * @param product
     * @param cluster
     * @return
     */
    @RequestMapping(value = "/{product}/{cluster}", method = RequestMethod.GET)
    public ResponseEntity getCluster(@PathVariable(name = "product") String product,
                                     @PathVariable(required = false) String cluster) {
        String productName = addressServerBuilderManager.generateProductName(product);
        String clusterName = addressServerManager.getDefaultClusterNameIfEmpty(cluster);

        String serviceName = addressServerBuilderManager.generateNacosServiceName(productName);
        Service service = serviceManager.getService(AddressServerConstants.DEFAULT_NAMESPACE, serviceName);
        if (service == null) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product=" + product + " not found.");
        }

        if (!service.getClusterMap().containsKey(clusterName)) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("product=" + product + ",cluster=" + clusterName + " not found.");
        }

        Cluster clusterObj = service.getClusterMap().get(clusterName);
        return ResponseEntity.status(HttpStatus.OK).body(addressServerBuilderManager.generateResponseIps(clusterObj.allIPs(false)));
    }
}