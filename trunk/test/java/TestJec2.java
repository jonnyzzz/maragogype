
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.DescribeImageAttributeResult;
import com.xerox.amazonws.ec2.GroupDescription;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.Jec2.ImageListAttributeOperationType;
import com.xerox.amazonws.ec2.ImageAttribute.ImageAttributeType;
import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.ImageListAttributeItem;
import com.xerox.amazonws.ec2.ImageListAttribute.ImageListAttributeItemType;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchPermissionAttribute;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import com.xerox.amazonws.tools.LoggingConfigurator;

public class TestJec2 {
    private static Logger log = LoggingConfigurator.configureLogging(TestJec2.class);

	public static void main(String [] args) throws Exception {
		Jec2 ec2 = new Jec2("1SEQ6QDW2YNW8T6K64R2", "7P1KY+a4FTtiVBuU935NHHOI19eYrbyWG7CDklmk", false, "localhost");
		List<String> params = new ArrayList<String>();
	
/*
		for (int i=0; i<10; i++) {
			long start = System.currentTimeMillis();
*/
			//params.add("291944132575");
			List<ImageDescription> images = ec2.describeImages(params);
			log.info("Available Images");
			for (ImageDescription img : images) {
				if (img.imageState.equals("available")) {
					log.info(img.imageId+"\t"+img.imageLocation+"\t"+img.imageOwnerId);
				}
			}
/*
			long end = System.currentTimeMillis();
			log.info("duration to find "+images.size()+" images = "+((end-start)/1000.0));
		}
*/

		params = new ArrayList<String>();
		List<ReservationDescription> instances = ec2.describeInstances(params);
		log.info("Instances");
		String instanceId = "";
		for (ReservationDescription res : instances) {
			log.info(res.owner+"\t"+res.resId+"\t"+res.owner);
			if (res.instances != null) {
				for (Instance inst : res.instances) {
					log.info("\t"+inst.imageId+"\t"+inst.dnsName+"\t"+inst.state+"\t"+inst.keyName);
					instanceId = inst.instanceId;
				}
			}
		}
		// test console output
/*
		ConsoleOutput consOutput = ec2.getConsoleOutput(instanceId);
		log.info("Console Output:");
		log.info(consOutput.output);
*/

		// test keypair methods
/*
		List<KeyPairInfo> info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.keyName+", "+i.keyFingerprint);
		}
		ec2.createKeyPair("test-keypair");
		info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.keyName+", "+i.keyFingerprint);
		}
		ec2.deleteKeyPair("test-keypair");
		info = ec2.describeKeyPairs(new String [] {});
		log.info("keypair list");
		for (KeyPairInfo i : info) {
			log.info("keypair : "+i.keyName+", "+i.keyFingerprint);
		}
*/

		// test security group methods
/*
		List<GroupDescription> info = ec2.describeSecurityGroups(new String [] {});
		log.info("SecurityGroup list");
		for (GroupDescription i : info) {
			log.info("group : "+i.name+", "+i.desc);
		}
		ec2.createSecurityGroup("test-group", "My test security group");
		info = ec2.describeSecurityGroups(new String [] {});
		log.info("SecurityGroup list");
		for (GroupDescription i : info) {
			log.info("group : "+i.name+", "+i.desc);
		}
		ec2.deleteSecurityGroup("test-group");
		info = ec2.describeSecurityGroups(new String [] {});
		log.info("GroupDescription list");
		for (GroupDescription i : info) {
			log.info("group : "+i.name+", "+i.desc);
		}
*/

		// test security group methods
/*
		DescribeImageAttributeResult res = ec2.describeImageAttribute("ami-5e836637", ImageAttributeType.launchPermission);
		Iterator<ImageListAttributeItem> iter = res.imageListAttribute.items.iterator();
		log.info("image attrs");
		while (iter.hasNext()) {
			ImageListAttributeItem item = iter.next();
			log.info("image : "+res.imageId+", "+item.value);
		}
		LaunchPermissionAttribute attr = new LaunchPermissionAttribute();
		attr.items.add(new ImageListAttributeItem(ImageListAttributeItemType.userId, "291944132575"));
		ec2.modifyImageAttribute("ami-5e836637", attr, ImageListAttributeOperationType.add);
		res = ec2.describeImageAttribute("ami-5e836637", ImageAttributeType.launchPermission);
		iter = res.imageListAttribute.items.iterator();
		log.info("image attrs");
		while (iter.hasNext()) {
			ImageListAttributeItem item = iter.next();
			log.info("image : "+res.imageId+", "+item.value);
		}
		ec2.resetImageAttribute("ami-5e836637", ImageAttributeType.launchPermission);
		res = ec2.describeImageAttribute("ami-5e836637", ImageAttributeType.launchPermission);
		iter = res.imageListAttribute.items.iterator();
		log.info("image attrs");
		while (iter.hasNext()) {
			ImageListAttributeItem item = iter.next();
			log.info("image : "+res.imageId+", "+item.value);
		}
*/
	}
}
