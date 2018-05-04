package withPipeline.onMaster

import groovy.mock.interceptor.MockFor
import org.junit.Test
import uk.gov.hmcts.contino.GradleBuilder
import uk.gov.hmcts.contino.JavaDeployer
import withPipeline.BaseCnpPipelineTest

class withJavaPipelineOnMasterTests extends BaseCnpPipelineTest {
  final static jenkinsFile = "exampleJavaPipeline.jenkins"

  withJavaPipelineOnMasterTests() {
    super("master", jenkinsFile)
  }

  @Test
  void PipelineExecutesExpectedStepsInExpectedOrder() {
    def mockBuilder = new MockFor(GradleBuilder)
    mockBuilder.demand.with {
      build(1) {}
      test(1) {}
      securityCheck(1) {}
      sonarScan(1) {}
      smokeTest(1) {} //aat-staging
      functionalTest(1) {}
      smokeTest(3) {} // aat-prod, prod-staging, prod-prod
    }

    def mockDeployer = new MockFor(JavaDeployer)
    mockDeployer.ignore.getServiceUrl() { env, slot -> return null} // we don't care when or how often this is called
    mockDeployer.demand.with {
      // aat-staging
      deploy() {}
      healthCheck() { env, slot -> return null }
      // aat-prod
      healthCheck() { env, slot -> return null }
      // prod-staging
      deploy() {}
      healthCheck() { env, slot -> return null }
      // prod-prod
      healthCheck() { env, slot -> return null }
    }

    mockBuilder.use {
      mockDeployer.use {
        runScript("testResources/$jenkinsFile")
      }
    }
  }
}

