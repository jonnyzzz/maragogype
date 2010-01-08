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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.xerox.amazonws.typica.jaxb.InstanceStateType;

/**
 * An instance of this class represents an EC2 instance slot reservation.
 * <p>
 * Instances are returned by calls to
 * {@link com.xerox.amazonws.ec2.Jec2#runInstances(String, int, int, List, String, String)},
 * {@link com.xerox.amazonws.ec2.Jec2#describeInstances(List)} and
 * {@link com.xerox.amazonws.ec2.Jec2#describeInstances(String[])}.
 */
public class ReservationDescription {
	private String requestId;
	private String owner;
	private String resId;
	private List<Instance> instances = new ArrayList<Instance>();
	private List<String> groups = new ArrayList<String>();

	public ReservationDescription(String requestId, String owner, String resId) {
		this.requestId = requestId;
		this.owner = owner;
		this.resId = resId;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getOwner() {
		return owner;
	}

	public String getReservationId() {
		return resId;
	}

	public Instance addInstance(String imageId, String instanceId,
			String privateDnsName, String dnsName, InstanceStateType state,
			String reason, String keyName, Calendar launchTime, InstanceType instanceType,
			String availabilityZone, String kernelId, String ramdiskId, String platform,
			boolean monitoring, String subnetId, String privateIpAddress, String ipAddress) {
		Instance instance = new Instance(imageId, instanceId, privateDnsName,
				dnsName, state.getName(), state.getCode(), reason, keyName,
				instanceType, launchTime, availabilityZone, kernelId, ramdiskId, platform,
				monitoring, subnetId, privateIpAddress, ipAddress);
		instances.add(instance);
		return instance;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public String addGroup(String groupId) {
		groups.add(groupId);
		return groupId;
	}

	public List<String> getGroups() {
		return groups;
	}

	/**
	 * Encapsulates information about an EC2 instance within a
	 * {@link ReservationDescription}.
	 */
	public class Instance {
		private String imageId;
		private String instanceId;
		private String privateDnsName;
		private String dnsName;
		private String reason;
		private String keyName;
		private InstanceType instanceType;
		private Calendar launchTime;
		private String availabilityZone;
		private String kernelId;
		private String ramdiskId;
		private String platform;

		/**
		 * An EC2 instance may be in one of four states:
		 * <ol>
		 * <li><b>pending</b> - the instance is in the process of being
		 * launched.</li>
		 * <li><b>running</b> - the has been launched. It may be in the
		 * process of booting and is not yet guaranteed to be reachable.</li>
		 * <li><b>shutting-down</b> - the instance is in the process of
		 * shutting down.</li>
		 * <li><b>terminated</b> - the instance is no longer running.</li>
		 * </ol>
		 */
		private String state;
		private int stateCode;
		private boolean monitoring;
		private String subnetId;
		private String privateIpAddress;
		private String ipAddress;

		public Instance(String imageId, String instanceId, String privateDnsName,
				String dnsName, String stateName, int stateCode, String reason,
				String keyName, InstanceType instanceType, Calendar launchTime,
				String availabilityZone, String kernelId, String ramdiskId, String platform,
				boolean monitoring, String subnetId, String privateIpAddress, String ipAddress) {
			this.imageId = imageId;
			this.instanceId = instanceId;
			this.privateDnsName = privateDnsName;
			this.dnsName = dnsName;
			this.state = stateName;
			this.stateCode = stateCode;
			this.reason = reason;
			this.keyName = keyName;
			this.instanceType = instanceType;
			this.launchTime = launchTime;
			this.availabilityZone = availabilityZone;
			this.kernelId = kernelId;
			this.ramdiskId = ramdiskId;
			this.platform = platform;
			this.monitoring = monitoring;
			this.subnetId = subnetId;
			this.privateIpAddress = privateIpAddress;
			this.ipAddress = ipAddress;
		}

		public String getImageId() {
			return imageId;
		}

		public String getInstanceId() {
			return instanceId;
		}

		public String getPrivateDnsName() {
			return privateDnsName;
		}

		public String getDnsName() {
			return dnsName;
		}

		public String getReason() {
			return reason;
		}

		public String getKeyName() {
			return keyName;
		}

		public String getState() {
			return state;
		}

		public boolean isRunning() {
			return this.state.equals("running");
		}

		public boolean isPending() {
			return this.state.equals("pending");
		}

		public boolean isShuttingDown() {
			return this.state.equals("shutting-down");
		}

		public boolean isTerminated() {
			return this.state.equals("terminated");
		}

		public int getStateCode() {
			return stateCode;
		}

		public InstanceType getInstanceType() {
			return this.instanceType;
		}

		public Calendar getLaunchTime() {
			return this.launchTime;
		}

		public String getAvailabilityZone() {
			return availabilityZone;
		}

		public String getKernelId() {
			return kernelId;
		}

		public String getRamdiskId() {
			return ramdiskId;
		}

		public String getPlatform() {
			return platform;
		}

		public boolean isMonitoring() {
			return monitoring;
		}

		public String getSubnetId() {
			return subnetId;
		}

		public String getPrivateIpAddress() {
			return privateIpAddress;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String toString() {
			return "[img=" + this.imageId + ", instance=" + this.instanceId
					+ ", privateDns=" + this.privateDnsName
					+ ", dns=" + this.dnsName + ", loc=" + ", state="
					+ this.state + "(" + this.stateCode + ") reason="
					+ this.reason + ", monitoring=" + this.monitoring
					+ ", subnetId=" + this.subnetId + "]";
		}
	}

	public String toString() {
		return "Reservation[id=" + this.resId + ", Loc=" + ", instances="
				+ this.instances + ", groups=" + this.groups + "]";
	}
}

