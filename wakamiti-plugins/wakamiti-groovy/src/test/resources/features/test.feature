Feature: Groovy Test Feature

  @ID-01
  Scenario: Execute script test
    Given that the following groovy code is executed:
      """groovy
      1+1
      """
    When the following groovy code is executed:
      """groovy
      log.info("Results: {}", ctx.results)
      assert !ctx.results.isEmpty()
      assert ctx.results[0] == 2
      """


  @ID-02
  Scenario: Execute script test 2
    When the following groovy code is executed:
      """groovy
      ctx['a'] = 'bbb'
      log.info("ctx: {}", ctx)
      assert ctx.results.isEmpty()
      """

  @ID-03
  Scenario: Execute script test 3
    Given that the following groovy code is executed:
      """groovy
      log.info("ctx step 1: {}", ctx)
      ctx['test'] = 33
      '[32]'
      """
    When the following groovy code is executed:
      """groovy
      log.info("ctx step 2: {}", ctx)
      def result = Eval.me('${1#}')[0]
      assert result == 32
      assert ctx['test'] == 33
      assert ctx['a'] == null
      assert ctx.id == 'ID-03'
      """
