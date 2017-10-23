import org.junit.Before

import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static uk.gov.hmcts.contino.ProjectSource.projectSource
import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Test

class withPipelineTest extends BasePipelineTest {

  // get the 'project' directory
  String projectDir = (new File(this.getClass().getResource("examplePipeline.jenkins").toURI())).parentFile.parentFile.parentFile.parentFile

  @Override
  @Before
  void setUp() {

    super.setUp()
    binding.setVariable("scm", null)
  }

  @Test
  void test1() {

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
    loadScript("testResources/examplePipeline.jenkins")
    printCallStack()
  }

}
