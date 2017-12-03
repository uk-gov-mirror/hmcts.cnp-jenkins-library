import uk.gov.hmcts.contino.ProjectBranch

/**
 * onPR
 *
 * Runs the block of code if the current branch is not master
 *
 * onPR {
 *   ...
 * }
 */
def call(block) {
  if (new ProjectBranch(env.BRANCH_NAME).isPR()) {
    return block.call()
  }
}
