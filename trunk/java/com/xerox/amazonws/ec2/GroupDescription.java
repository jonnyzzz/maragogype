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

/**
 * An instance of this class represents an EC2 security group.
 * <p>
 * Instances are returned by calls to
 * {@link Jec2#describeSecurityGroups(List)}, and
 * {@link Jec2#describeSecurityGroups(String[])}.
 */
public class GroupDescription {
	public String name;

	public String desc;

	public String owner;

	public List<IpPermission> perms = new ArrayList<IpPermission>();

	public GroupDescription(String name, String desc, String owner) {
		this.name = name;
		this.desc = desc;
		this.owner = owner;
	}

	public IpPermission addPermission(String protocol, int fromPort,
			int toPort) {
		IpPermission perm = new IpPermission(protocol, fromPort, toPort);
		perms.add(perm);
		return perm;
	}

	public class IpPermission {
		public String protocol;

		public int fromPort;

		public int toPort;

		public List<String> cidrIps = new ArrayList<String>();

		public List<String[]> uid_group_pairs = new ArrayList<String[]>();
		
		public IpPermission(String protocol, int fromPort, int toPort) {
			this.protocol = protocol;
			this.fromPort = fromPort;
			this.toPort = toPort;
		}

		public void addIpRange(String cidrIp) {
			this.cidrIps.add(cidrIp);
		}

		public void addUserGroupPair(String userId, String groupName) {
			this.uid_group_pairs.add(new String[] { userId, groupName });
		}

		public String toString() {
			List<String> uid_grp_str = new ArrayList<String>();
			for (String[] pair : this.uid_group_pairs) {
				uid_grp_str.add("(" + pair[0] + "," + pair[1] + ")");
			}
			return "[proto=" + this.protocol + ", portRng=("
					+ this.fromPort + ".." + this.toPort + "), cidrIps="
					+ this.cidrIps + ", uidgrp=" + uid_grp_str
					+ "]";
		}
	}

	public String toString() {
		return "Group[name=" + this.name + ", Desc=" + this.desc + ", own="
				+ this.owner + ", perms=" + this.perms + "]";
	}
}

