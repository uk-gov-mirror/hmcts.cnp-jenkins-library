package uk.gov.hmcts.contino

interface Builder {
  def build()
  def test()
  def smokeTest()
  def securityCheck()
}