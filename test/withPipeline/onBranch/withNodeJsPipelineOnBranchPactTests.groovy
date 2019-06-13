package withPipeline.onBranch

import com.lesfurets.jenkins.unit.BasePipelineTest
import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import org.junit.Test
import uk.gov.hmcts.contino.*

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static uk.gov.hmcts.contino.ProjectSource.projectSource

class withNodeJsPipelineOnBranchPactTests extends BasePipelineTest {
  final static jenkinsFile = "exampleNodeJsPipelineForPact.jenkins"

  // get the 'project' directory
  String projectDir = (new File(this.getClass().getClassLoader().getResource(jenkinsFile).toURI())).parentFile.parentFile.parentFile.parentFile

  withNodeJsPipelineOnBranchPactTests() {
    super.setUp()
    binding.setVariable("scm", null)
    binding.setVariable("env", [BRANCH_NAME: "feature-branch"])
    binding.setVariable("Jenkins", [instance: new MockJenkins(new MockJenkinsPluginManager([new MockJenkinsPlugin('sonar', true) ] as MockJenkinsPlugin[]))])

    def library = library()
      .name('Infrastructure')
      .targetPath(projectDir)
      .retriever(projectSource(projectDir))
      .defaultVersion("master")
      .allowOverride(true)
      .implicit(false)
      .build()
    helper.registerSharedLibrary(library)
    helper.registerAllowedMethod("deleteDir", {})
    helper.registerAllowedMethod("stash", [Map.class], {})
    helper.registerAllowedMethod("unstash", [String.class], {})
    helper.registerAllowedMethod("withEnv", [List.class, Closure.class], {})
    helper.registerAllowedMethod("ansiColor", [String.class, Closure.class], { s, c -> c.call() })
    helper.registerAllowedMethod("withCredentials", [LinkedHashMap, Closure.class], { c -> c.call()})
    helper.registerAllowedMethod("azureServicePrincipal", [LinkedHashMap, Closure.class], { c -> c.call()})
    helper.registerAllowedMethod("sh", [Map.class], { return "" })
    helper.registerAllowedMethod('fileExists', [String.class], { c -> true })
    helper.registerAllowedMethod("timestamps", [Closure.class], { c -> c.call() })
    helper.registerAllowedMethod("withSonarQubeEnv", [String.class, Closure.class], { s, c -> c.call() })
    helper.registerAllowedMethod("waitForQualityGate", { [status: 'OK'] })
  }

  @Test
  void PipelineExecutesExpectedSteps() {
    def stubBuilder = new StubFor(YarnBuilder)
    def stubPactBroker = new StubFor(PactBroker)

    stubBuilder.demand.with {
      setupToolVersion(1) {}
      build(1) {}
      test(1) {}
      sonarScan(1) {}
      securityCheck(1) {}
      runConsumerTests(1) { url, version -> return null }
      runProviderVerification(0) { url, version -> return null }
    }

    stubPactBroker.demand.with {
      canIDeploy(0) { version -> return null }
    }


    // ensure no deployer methods are called
    def mockDeployer = new MockFor(NodeDeployer)

    mockDeployer.use {
      stubBuilder.use {
        stubPactBroker.use {
          runScript("testResources/$jenkinsFile")
        }
      }
    }

    stubBuilder.expect.verify()
    stubPactBroker.expect.verify()
  }
}
