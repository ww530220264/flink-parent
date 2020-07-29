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

package org.apache.flink.runtime.jobgraph;

import org.apache.flink.runtime.executiongraph.ExecutionVertex;

/**
 * A distribution pattern determines, which sub tasks of a producing task are connected to which
 * consuming sub tasks. // 分布式模式决定了生产任务的哪些子任务与消费者子任务相连接
 */
public enum DistributionPattern {

	/**每个生产子任务与每个消费任务的子任务相连接-->多对多
	 * Each producing sub task is connected to each sub task of the consuming task.
	 * <p>
	 * {@link ExecutionVertex#connectAllToAll(org.apache.flink.runtime.executiongraph.IntermediateResultPartition[], int)}
	 */
	ALL_TO_ALL,

	/**每个生产子任务都连接到消费任务的一个或多个子任务--一对一或一对多
	 * Each producing sub task is connected to one or more subtask(s) of the consuming task.
	 * <p>
	 * {@link ExecutionVertex#connectPointwise(org.apache.flink.runtime.executiongraph.IntermediateResultPartition[], int)}
	 */
	POINTWISE
}
