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

import com.xerox.amazonws.typica.jaxb.EbsInstanceBlockDeviceMappingResponseType;
import com.xerox.amazonws.typica.jaxb.GroupItemType;
import com.xerox.amazonws.typica.jaxb.GroupSetType;
import com.xerox.amazonws.typica.jaxb.InstanceBlockDeviceMappingResponseType;
import com.xerox.amazonws.typica.jaxb.InstanceBlockDeviceMappingResponseItemType;
import com.xerox.amazonws.typica.jaxb.InstanceStateType;
import com.xerox.amazonws.typica.jaxb.ProductCodesSetItemType;
import com.xerox.amazonws.typica.jaxb.RunningInstancesItemType;
import com.xerox.amazonws.typica.jaxb.RunningInstancesSetType;

/**
 * An instance of this class represents an EC2 instance slot reservation.
 * <p/>
 * Instances are returned by calls to
 * {@link com.xerox.amazonws.ec2.Jec2#runInstances(String, int, int, List, String, String)},
 * {@link com.xerox.amazonws.ec2.Jec2#describeInstances(List)} and
 * {@link com.xerox.amazonws.ec2.Jec2#describeInstances(String[])}.
 */
public class ReservationDescription {
  private final String requestId;
  private final String owner;
  private final String resId;
  private final String requesterId;
  private final List<Instance> instances = new ArrayList<Instance>();
  private final List<String> groups = new ArrayList<String>();

  public ReservationDescription(String requestId,
                                String ownerId,
                                String reservationId,
                                String requesterId,
                                GroupSetType groupSet,
                                RunningInstancesSetType instSet) {
    this.requestId = requestId;
    this.owner = ownerId;
    this.resId = reservationId;
    this.requesterId = requesterId;

    for (GroupItemType rsp_item : groupSet.getItems()) {
      groups.add(rsp_item.getGroupId());
    }
    for (RunningInstancesItemType rsp_item : instSet.getItems()) {
      instances.add(new Instance(rsp_item));
    }
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

  public String getRequesterId() {
    return requesterId;
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
    private final String imageId;
    private final String instanceId;
    private final String privateDnsName;
    private final String dnsName;
    private final String reason;
    private final String keyName;
    private final String launchIndex;
    private final List<String> productCodes;
    private final InstanceType instanceType;
    private final Calendar launchTime;
    private final String availabilityZone;
    private final String kernelId;
    private final String ramdiskId;
    private final String platform;
    private final String clientToken;

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
    private final String state;
    private final String stateCode;
    private final boolean monitoring;
    private final String subnetId;
    private final String vpcId;
    private final String privateIpAddress;
    private final String ipAddress;
    private final String architecture;
    private final String rootDeviceType;
    private final String rootDeviceName;
    private final List<InstanceBlockDeviceMapping> blockDeviceMapping;
    private final String instanceLifecycle;
    private final String spotInstanceRequestId;
    private final String virtualizationType;

    public Instance(RunningInstancesItemType rsp_item) {
      this.imageId = rsp_item.getImageId();
      this.instanceId = rsp_item.getInstanceId();
      this.privateDnsName = rsp_item.getPrivateDnsName();
      this.dnsName = rsp_item.getDnsName();
      this.state = rsp_item.getInstanceState().getName();
      this.stateCode = "" + rsp_item.getInstanceState().getCode();
      if (rsp_item.getStateReason() != null) {
        this.reason = rsp_item.getStateReason().getMessage();
      } else {
        this.reason = "";
      }
      this.keyName = rsp_item.getKeyName();
      this.launchIndex = rsp_item.getAmiLaunchIndex();
      ArrayList<String> codes = new ArrayList<String>();
      for (ProductCodesSetItemType type : rsp_item.getProductCodes().getItems()) {
        codes.add(type.getProductCode());
      }
      this.productCodes = codes;
      this.instanceType = InstanceType.getTypeFromString(rsp_item.getInstanceType());
      this.launchTime = rsp_item.getLaunchTime().toGregorianCalendar();
      this.availabilityZone = rsp_item.getPlacement().getAvailabilityZone();
      this.kernelId = rsp_item.getKernelId();
      this.ramdiskId = rsp_item.getRamdiskId();
      this.platform = rsp_item.getPlatform();
      this.monitoring = rsp_item.getMonitoring().getState().contains("enabled");
      this.subnetId = rsp_item.getSubnetId();
      this.vpcId = rsp_item.getVpcId();
      this.privateIpAddress = rsp_item.getPrivateIpAddress();
      this.ipAddress = rsp_item.getIpAddress();
      this.architecture = rsp_item.getArchitecture();
      this.rootDeviceType = rsp_item.getRootDeviceType();
      this.rootDeviceName = rsp_item.getRootDeviceName();
      this.blockDeviceMapping = new ArrayList<InstanceBlockDeviceMapping>();
      InstanceBlockDeviceMappingResponseType bdmType = rsp_item.getBlockDeviceMapping();
      if (bdmType != null) {
        List<InstanceBlockDeviceMappingResponseItemType> bdmSet = bdmType.getItems();
        if (bdmSet != null) {
          for (InstanceBlockDeviceMappingResponseItemType mapping : bdmSet) {
            EbsInstanceBlockDeviceMappingResponseType ebs = mapping.getEbs();
            this.blockDeviceMapping.add(new InstanceBlockDeviceMapping(mapping.getDeviceName(), ebs.getVolumeId(),
                    ebs.getStatus(), ebs.getAttachTime().toGregorianCalendar(),
                    ebs.isDeleteOnTermination()));
          }
        }
      }
      this.instanceLifecycle = rsp_item.getInstanceLifecycle();
      this.spotInstanceRequestId = rsp_item.getSpotInstanceRequestId();
      this.virtualizationType = rsp_item.getVirtualizationType();
      this.clientToken = rsp_item.getClientToken();
    }

    public String getImageId() {
      return imageId;
    }

    public String getInstanceId() {
      return instanceId;
    }

    public String getClientToken() {
      return clientToken;
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

    public String getLaunchIndex() {
      return launchIndex;
    }

    public List<String> getProductCodes() {
      return productCodes;
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

    public String getStateCode() {
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

    public String getVpcId() {
      return vpcId;
    }

    public String getPrivateIpAddress() {
      return privateIpAddress;
    }

    public String getIpAddress() {
      return ipAddress;
    }

    public String getArchitecture() {
      return architecture;
    }

    public String getRootDeviceType() {
      return rootDeviceType;
    }

    public String getRootDeviceName() {
      return rootDeviceName;
    }

    public List<InstanceBlockDeviceMapping> getBlockDeviceMappings() {
      return blockDeviceMapping;
    }

    public String getInstanceLifecycle() {
      return instanceLifecycle;
    }

    public String getSpotInstanceRequestId() {
      return spotInstanceRequestId;
    }

    public String getVirtualizationType() {
      return virtualizationType;
    }

    public String toString() {
      return "[img=" + this.imageId + ", instance=" + this.instanceId
              + ", privateDns=" + this.privateDnsName
              + ", dns=" + this.dnsName + ", loc=" + this.availabilityZone + ", state="
              + this.state + "(" + this.stateCode + ") reason="
              + this.reason + ", monitoring=" + this.monitoring
              + ", subnetId=" + this.subnetId + ", clientToken=" + clientToken + "]";
    }
  }

  public String toString() {
    return "Reservation[id=" + this.resId + ", Loc=" + ", instances="
            + this.instances + ", groups=" + this.groups + "]";
  }
}

