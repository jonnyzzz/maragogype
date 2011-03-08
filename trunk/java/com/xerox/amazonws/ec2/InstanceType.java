//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.xerox.amazonws.ec2;

/**
 * This enumeration represents different instance types that can be launched.
 */
public enum InstanceType {
	SMALL ("m1.small", InstancePlatform.X86),
	LARGE ("m1.large", InstancePlatform.X64),
	XLARGE ("m1.xlarge", InstancePlatform.X64),
  MICRO("t1.micro", InstancePlatform.ANY),
	MEDIUM_HCPU ("c1.medium", InstancePlatform.X86),
	XLARGE_HCPU ("c1.xlarge", InstancePlatform.X64),
	XLARGE_HMEM ("m2.xlarge", InstancePlatform.X64),
	XLARGE_DOUBLE_HMEM ("m2.2xlarge", InstancePlatform.X64),
	XLARGE_QUAD_HMEM ("m2.4xlarge", InstancePlatform.X64),
	XLARGE_CLUSTER_COMPUTE ("cc1.4xlarge", InstancePlatform.X64),
  XLARGE_CLUSTER_GPU_COMPUTE("cg1.4xlarge", InstancePlatform.X64)
  ;

  public static final InstanceType DEFAULT = SMALL;
  private final String typeId;
  private final InstancePlatform instancePlatform;

	InstanceType(String typeId, InstancePlatform instancePlatform) {
		this.typeId = typeId;
    this.instancePlatform = instancePlatform;
  }

	public String getTypeId() {
		return typeId;
	}

  public InstancePlatform getInstancePlatform() {
    return instancePlatform;
  }

  public static InstanceType getTypeFromString(String val) {
		for (InstanceType t : InstanceType.values()) {
			if (t.getTypeId().equals(val)) {
				return t;
			}
		}
		return null;
	}
}
