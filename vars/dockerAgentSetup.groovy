import uk.gov.hmcts.contino.azure.KeyVault

def call() {
  if (env.IS_DOCKER_BUILD_AGENT && env.IS_DOCKER_BUILD_AGENT.toBoolean()) {
    def envName = env.JENKINS_SUBSCRIPTION_NAME == "DTS-CFTSBOX-INTSVC" ? "sandbox" : "prod"
    echo "Using container env: ${envName}"
    // Check github host key
    int githubHostKeyCheck = sh(script: "grep '^github.com ssh-rsa' /home/jenkins/.ssh/known_hosts > /dev/null", returnStatus: "true")
    if (githubHostKeyCheck != 0) {
      sh """
        ssh-keyscan -t rsa github.com >> /home/jenkins/.ssh/known_hosts
        chmod 644 /home/jenkins/.ssh/known_hosts
      """
    }
    // Check github private key
    String infraVaultName = env.INFRA_VAULT_NAME
    String sshFile = "/home/jenkins/.ssh/id_rsa"
    String vaultSecret = "jenkins-ssh-private-key"
    String idRsa = "${envName}-${infraVaultName}-${vaultSecret}"
    if (env.CURRENT_ID_RSA != idRsa || !fileExists(sshFile)) {
      withSubscriptionLogin(envName) {
        KeyVault keyVault = new KeyVault(this, envName, infraVaultName)
        keyVault.download(vaultSecret, sshFile, "600")
        env.CURRENT_ID_RSA = idRsa
      }
    } else {
      echo "Using existing ssh id_rsa"
    }
  }
  // Add testcontainers config
  if (env.TESTCONTAINERS_HOST_OVERRIDE == null || "".equals(env.TESTCONTAINERS_HOST_OVERRIDE)) {
    def response = httpRequest(
      consoleLogResponseBody: true,
      timeout: 10,
      url: "http://169.254.169.254/metadata/instance/network/interface/0/ipv4/ipAddress/0?api-version=2020-06-01",
      customHeaders: [[name: 'Metadata', value: 'true']],
      validResponseCodes: '200'
    )
    def instanceMetadata = readJSON(text: response.content)
    env.TESTCONTAINERS_HOST_OVERRIDE = instanceMetadata.privateIpAddress
    env.DOCKER_IP = instanceMetadata.privateIpAddress
    env.DOCKER_HOST = "tcp://${instanceMetadata.privateIpAddress}:2375"
    echo "TESTCONTAINERS_HOST_OVERRIDE: ${env.TESTCONTAINERS_HOST_OVERRIDE}"
  }
}

