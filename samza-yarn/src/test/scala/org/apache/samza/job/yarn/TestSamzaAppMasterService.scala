/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.samza.job.yarn

import java.io.BufferedReader
import java.net.URL
import java.io.InputStreamReader

import org.apache.hadoop.yarn.util.ConverterUtils
import org.apache.samza.config.MapConfig
import org.junit.Assert._
import org.junit.Test

import scala.collection.JavaConversions._

class TestSamzaAppMasterService {
  @Test
  def testAppMasterDashboardShouldStart {
    val state = new SamzaAppMasterState(-1, ConverterUtils.toContainerId("container_1350670447861_0003_01_000002"), "", 1, 2)
    val service = new SamzaAppMasterService(null, state, null, null)

    // start the dashboard
    service.onInit
    assertTrue(state.rpcPort > 0)
    assertTrue(state.trackingPort > 0)

    // check to see if it's running
    val url = new URL("http://127.0.0.1:%d/am" format state.rpcPort)
    val is = url.openConnection().getInputStream();
    val reader = new BufferedReader(new InputStreamReader(is));
    var line: String = null;

    do {
      line = reader.readLine()
    } while (line != null)

    reader.close();
  }

  /**
   * This tests the rendering of the index.scaml file containing some Scala code. The objective
   * is to ensure that the rendered scala code builds correctly
   */
  @Test
  def testAppMasterDashboardWebServiceShouldStart {

    // Create some dummy config
    val config = new MapConfig(Map[String, String](
      "yarn.container.count" -> "1",
      "systems.test-system.samza.factory" -> "org.apache.samza.job.yarn.MockSystemFactory",
      "yarn.container.memory.mb" -> "512",
      "yarn.package.path" -> "/foo",
      "task.inputs" -> "test-system.test-stream",
      "systems.test-system.samza.key.serde" -> "org.apache.samza.serializers.JsonSerde",
      "systems.test-system.samza.msg.serde" -> "org.apache.samza.serializers.JsonSerde",
      "yarn.container.retry.count" -> "1",
      "yarn.container.retry.window.ms" -> "1999999999"))


    val state = new SamzaAppMasterState(-1, ConverterUtils.toContainerId("container_1350670447861_0003_01_000002"), "", 1, 2)
    val service = new SamzaAppMasterService(config, state, null, null)

    // start the dashboard
    service.onInit
    assertTrue(state.rpcPort > 0)
    assertTrue(state.trackingPort > 0)

    // Do a GET Request on the tracking port: This in turn will render index.scaml
    val url = new URL("http://127.0.0.1:%d/" format state.trackingPort)
    val is = url.openConnection().getInputStream();
    val reader = new BufferedReader(new InputStreamReader(is));
    var line: String = null;

    do {
      line = reader.readLine()
    } while (line != null)

    reader.close();
  }

}
