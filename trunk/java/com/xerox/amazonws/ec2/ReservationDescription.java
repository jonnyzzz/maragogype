
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
	public String owner;

	public String resId;

	public List<Instance> instances = new ArrayList<Instance>();

	public List<String> groups = new ArrayList<String>();

	public ReservationDescription(String owner, String resId) {
		this.owner = owner;
		this.resId = resId;
	}

	public Instance addInstance(String imageId, String instanceId,
			String dnsName, InstanceStateType state, String reason,
			String keyName) {
		Instance instance = new Instance(imageId, instanceId, dnsName,
				state.getName(), state.getCode(), reason, keyName);
		instances.add(instance);
		return instance;
	}

	public String addGroup(String groupId) {
		groups.add(groupId);
		return groupId;
	}

	/**
	 * Encapsulates information about an EC2 instance within a
	 * {@link Jec2.ReservationDescription}.
	 */
	public class Instance {
		public String imageId;

		public String instanceId;

		public String dnsName;

		public String reason;

		public String keyName;

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
		public String state;

		public int stateCode;

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

