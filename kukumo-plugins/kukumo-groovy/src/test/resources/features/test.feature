Feature: Groovy Test Feature


  Scenario: Execute script test
    Given that the following groovy code is executed:
      """groovy
      1+1
      """
    When the following groovy code is executed:
      """groovy
      log.info("Steps: {}", ctx.backend().stepBackendData)
      log.info("Results: {}", ctx.backend().getResults())
      assert !ctx.backend().getResults().isEmpty()
      assert ctx.backend().getResults()[0] == 2
      """

  Scenario: Execute script test 2
    When the following groovy code is executed:
      """groovy
      log.info("Steps: {}", ctx.backend().stepBackendData)
      log.info("Results: {}", ctx.backend().getResults())
      assert ctx.backend().getResults().isEmpty()
      """