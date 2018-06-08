package uk.gov.hmcts.contino

class Gatling implements Serializable {

  public static final String GATLING_REPORTS_DIR  = 'build/gatling/reports'
  public static final String GATLING_SIMS_DIR     = 'src/gatling/simulations'
  public static final String GATLING_DATA_DIR     = 'src/gatling/data'
  public static final String GATLING_BODIES_DIR   = 'src/gatling/bodies'
  public static final String GATLING_CONF_DIR     = 'src/gatling/conf'
  public static final String GATLING_IMAGE        = 'hmcts/moj-gatling-image:2.3.1-1.0'

  // with Gatling command-line you can't specify a configuration directory, so we need to bind-mount it
  public static final String GATLING_RUN_ARGS     = '-u root -v $WORKSPACE/'+ GATLING_CONF_DIR + ':/etc/gatling/conf'

  def steps

  Gatling(steps) {
    this.steps = steps
  }

  def execute() {
    this.steps.withDocker(GATLING_IMAGE, GATLING_RUN_ARGS) {
      this.steps.sh "gatling.sh -m -sf ${GATLING_SIMS_DIR} -df ${GATLING_DATA_DIR} -bdf ${GATLING_BODIES_DIR} -rf ${GATLING_REPORTS_DIR}"
    }
  }

}