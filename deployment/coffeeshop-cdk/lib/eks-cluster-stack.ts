import * as cdk from '@aws-cdk/core';
import * as iam from '@aws-cdk/aws-iam';
import * as eks from '@aws-cdk/aws-eks';
import * as ec2 from '@aws-cdk/aws-ec2';
import { PhysicalName } from '@aws-cdk/core';
import { Vpc } from '@aws-cdk/aws-ec2';

export class EKSClusterStack extends cdk.Stack {
	public readonly eksCluster: eks.Cluster;
	public readonly deployRole: iam.Role;

	constructor(scope: cdk.Construct, id: string, vpc?: ec2.Vpc, props?: cdk.StackProps) {
		super(scope, id, props);

		// const region = defaultRegion || 'ap-northeast-2';		// Default region.

		const eksClusterRole = new iam.Role(
			this,
			`${id}-AdminRole`,
			// {assumedBy: new iam.AccountRootPrincipal}
			// Or, use this account principal.
			{assumedBy: new iam.AccountPrincipal(this.account)}
		);

		/**
		 * [2021-03-14] KSH:
		 * TODO: Separate cluster name from ID.
		 */
		const eksCluster = new eks.Cluster(
			this,
			id,
			{
				clusterName: id,
				version: eks.KubernetesVersion.V1_19,
				mastersRole: eksClusterRole,
				defaultCapacity: 2,
				// [2021-03-14]: TODO: Read from SSM or props?.vpcAttributes at least.
				vpc: vpc
			}
		);

		/**
		 * [2021-03-14]: KSH:
		 * TODO: Comment out if you don't want to set this cluster group as spot instance.
		 */
		eksCluster.addAutoScalingGroupCapacity(
			`${id}-ASG`,
			{
				instanceType: new ec2.InstanceType('m5.xlarge'),
				spotPrice: '0.248'
			}
		);

    // Create OIDC Provider
    const provider = new eks.OpenIdConnectProvider(
      this, 'CoffeeShopEKSClusterOIDCProvider', {
        url: eksCluster.clusterOpenIdConnectIssuerUrl
      }
    );

		this.eksCluster = eksCluster;


	/**
	 * [2021-03-15] KSH:
	 * Add existing SSO role to masters role.
	 * TODO: Parameterize.
	 */
		const ssoRole = 'arn:aws:iam::301391518739:role/aws-reserved/sso.amazonaws.com/ap-southeast-1/AWSReservedSSO_AdministratorAccess_961399e45c4f66a3';
		this.addExistingRoleToMastersRole(eksCluster, ssoRole.split('/').pop() + '', ssoRole);


		const deployRole = createDeployRole(this, 'deploy-role', eksCluster, this.account);
	}

  addExistingRoleToMastersRole(cluster: eks.Cluster, id: string, roleArn: string) {
    // Add existingrole to aws-auth as a MasterRole
    const role = iam.Role.fromRoleArn(this, id, roleArn, {
      // Set 'mutable' to 'false' to use the role as-is and prevent adding new
      // policies to it. The default is 'true', which means the role may be
      // modified as part of the deployment.
      mutable: false,
    });
    cluster.awsAuth.addMastersRole(role);
  }

  addExistingUserToMastersRole(cluster: eks.Cluster, id: string, userName: string) {
    // Add existingrole to aws-auth as a MasterRole
    const adminUser = iam.User.fromUserName(this, id, userName);
    cluster.awsAuth.addUserMapping(adminUser, { groups: [ 'system:masters' ]});
  }
}

function createDeployRole(scope: cdk.Construct, id: string, eksCluster: eks.Cluster, account?: string): iam.Role {
	const role = new iam.Role(scope, id, {
		roleName: PhysicalName.GENERATE_IF_NEEDED,
		assumedBy: (account ? new iam.AccountPrincipal(account) : new iam.AccountRootPrincipal())
	});

	eksCluster.awsAuth.addMastersRole(role);

	return role;
}