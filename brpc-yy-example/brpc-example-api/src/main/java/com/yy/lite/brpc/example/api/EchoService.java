/*
 * Copyright (c) 2018 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yy.lite.brpc.example.api;

import com.baidu.brpc.protocol.BrpcMeta;
import com.yy.anka.io.codec.protocol.bean.MChannel;

/**
 * Created by huwenwei on 2018/11/23.
 */
public interface EchoService {

    @BrpcMeta(serviceName = "__" + (9 << 8 | 278), methodName = "" + (10 << 8 | 278))
    EchoResponse echo(EchoRequest request);

    @BrpcMeta(serviceName = "464__7134_1041", methodName = "7134_1042")
    EchoResponse echo2(MChannel<EchoRequest> request);


}
