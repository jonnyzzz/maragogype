//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//

package com.xerox.amazonws.ec2;

import java.util.ArrayList;
import java.util.List;

import com.xerox.amazonws.typica.jaxb.InstanceStateType;

/**
 * An instance of this class represents an EC2 instance slot reservation.
 * <p>
 * Instances are returned by calls to
 * {@link Jec2#runInstances(String, int, int, List, String)},
 * {@link Jec2#DescribeInstances(List)} and
 * {@link Jec2#DescribeInstances(String[])}.
 */
public class ReservationDescription {
	private String owner;
	private String resId;
	private List<Instance> instances = new ArrayList<Instance>();
	private List<String> groups = new ArrayList<String>();

	public ReservationDescription(String owner, String resId) {
		this.owner = owner;
		this.resId = resId;
	}

	public String getOwner() {
		return owner;
	}

	public String getReservationId() {
		return resId;
	}

	public Instance addInstance(String imageId, String instanceId,
			String dnsName, InstanceStateType state, String reason,
			String keyName) {
		Instance instance = new Instance(imageId, instanceId, dnsName,
				state.getName(), state.getCode(), reason, keyName);
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
	 * {@link Jec2.ReservationDescription}.
	 */
	public class Instance {
		private String imageId;
		private String instanceId;
		private String dnsName;
		private String reason;
		private String keyName;
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

		public Instance(String imageId, String instanceId, String dnsName,
				String stateName, int stateCode, String reason,
				String keyName) {
			this.imageId = imageId;
			this.instanceId = instanceId;
			this.dnsName = dnsName;
			this.state = stateName;
			this.stateCode = stateCode;
			this.reason = reason;
			this.keyName = keyName;
		}

		public String getImageId() {
			return imageId;
		}

		public String getInstanceId() {
			return instanceId;
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
			return this.state.equalsIgnoreCase("running");
		}

		public boolean isPending() {
			return this.state.equalsIgnoreCase("pending");
		}

		public boolean isShuttingDown() {
			return this.state.equalsIgnoreCase("shutting-down");
		}

		public boolean isTerminated() {
			return this.state.equalsIgnoreCase("terminated");
		}

		public int getStateCode() {
			return stateCode;
		}

		public String toString() {
			return "[img=" + this.imageId + ", instance=" + this.instanceId
					+ ", dns=" + this.dnsName + ", loc=" + ", state="
					+ this.state + "(" + this.stateCode + ") reason="
					+ this.reason + "]";
		}
	}

	public String toString() {
		return "Reservation[id=" + this.resId + ", Loc=" + ", instances="
				+ this.instances + ", groups=" + this.groups + "]";
	}
}

