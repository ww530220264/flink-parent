/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.streaming.api.scala

import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.runtime.jobgraph.JobGraph
import org.junit.Assert._
import org.junit.Test


/**
 * This verifies that slot sharing groups are correctly forwarded from user job to JobGraph.
 *
 * These tests also implicitly verify that chaining does not work across
 * resource groups/slot sharing groups.
 */
class SlotAllocationTest {

  @Test
  def testSlotGroups(): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val dummyFilter = new FilterFunction[Long]() {
      def filter(value: Long): Boolean = {
        false
      }
    }

    env.generateSequence(1, 10) // 1
      .filter(dummyFilter).slotSharingGroup("isolated") // 7
      .filter(dummyFilter).slotSharingGroup("default").disableChaining()//8
      .filter(dummyFilter).slotSharingGroup("group 1") // 9
      .filter(dummyFilter) // 10
      .startNewChain()
      .print().disableChaining() // 11

    // verify that a second pipeline does not inherit the groups from the first pipeline
    env.generateSequence(1, 10) // 0
      .filter(dummyFilter).slotSharingGroup("isolated-2") // 2
      .filter(dummyFilter).slotSharingGroup("default").disableChaining() // 3
      .filter(dummyFilter).slotSharingGroup("group 2") // 4
      .filter(dummyFilter) // 5
      .startNewChain()
      .print().disableChaining() // 6

    val jobGraph: JobGraph = env.getStreamGraph.getJobGraph

    val vertices = jobGraph.getVerticesSortedTopologicallyFromSources

    assertEquals(vertices.get(0).getSlotSharingGroup, vertices.get(3).getSlotSharingGroup)
    assertNotEquals(vertices.get(0).getSlotSharingGroup, vertices.get(2).getSlotSharingGroup)
    assertNotEquals(vertices.get(3).getSlotSharingGroup, vertices.get(4).getSlotSharingGroup)
    assertEquals(vertices.get(4).getSlotSharingGroup, vertices.get(5).getSlotSharingGroup)
    assertEquals(vertices.get(5).getSlotSharingGroup, vertices.get(6).getSlotSharingGroup)

    val s: Int = 6
    assertEquals(vertices.get(1).getSlotSharingGroup, vertices.get(s + 2).getSlotSharingGroup)
    assertNotEquals(vertices.get(1).getSlotSharingGroup, vertices.get(s + 1).getSlotSharingGroup)
    assertNotEquals(
      vertices.get(s + 2).getSlotSharingGroup,
      vertices.get(s + 3).getSlotSharingGroup)
    assertEquals(vertices.get(s + 3).getSlotSharingGroup, vertices.get(s + 4).getSlotSharingGroup)
    assertEquals(vertices.get(s + 4).getSlotSharingGroup, vertices.get(s + 5).getSlotSharingGroup)
  }
}
