package com.xerox.amazonws.vpc;

/**
 * Description of Amazon VPC subnet
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 14.03.11 20:29
 */
public class SubnetInfo {
    private final String subnetId;
    private final String state;
    private final String vpcId;
    private final String cidrBlock;
    private final Integer availableIpAddressCount;
    private final String availabilityZone;

  public SubnetInfo(String subnetId, String state, String vpcId, String cidrBlock, Integer availableIpAddressCount, String availabilityZone) {
    this.subnetId = subnetId;
    this.state = state;
    this.vpcId = vpcId;
    this.cidrBlock = cidrBlock;
    this.availableIpAddressCount = availableIpAddressCount;
    this.availabilityZone = availabilityZone;
  }

  public String getSubnetId() {
    return subnetId;
  }

  public String getState() {
    return state;
  }

  public String getVpcId() {
    return vpcId;
  }

  public String getCidrBlock() {
    return cidrBlock;
  }

  public Integer getAvailableIpAddressCount() {
    return availableIpAddressCount;
  }

  public String getAvailabilityZone() {
    return availabilityZone;
  }
}
