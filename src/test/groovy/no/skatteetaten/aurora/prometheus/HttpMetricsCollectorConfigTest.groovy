package no.skatteetaten.aurora.prometheus

import static no.skatteetaten.aurora.prometheus.HttpMetricsCollectorConfig.MetricsMode.ALL
import static no.skatteetaten.aurora.prometheus.HttpMetricsCollectorConfig.MetricsMode.EXCLUDE
import static no.skatteetaten.aurora.prometheus.HttpMetricsCollectorConfig.MetricsMode.INCLUDE
import static no.skatteetaten.aurora.prometheus.HttpMetricsCollectorConfig.MetricsMode.INCLUDE_MAPPINGS

import java.util.regex.PatternSyntaxException

import spock.lang.Specification
import spock.lang.Unroll

class HttpMetricsCollectorConfigTest extends Specification {

  @Unroll
  def "url #url in #mode should be #match"() {

    expect:
      def config = new HttpMetricsCollectorConfig(mode,
          ["foo": ".*foo.com.*"], ["bar": ".*bar.com.*"], ["baz": ".*baz.com.*"])
      config.shouldRecord(url) == match


    where:
      url                  | mode             | match
      "http://foo.com/api" | INCLUDE_MAPPINGS | true
      "http://bar.com/api" | INCLUDE_MAPPINGS | false
      "http://foo.com/api" | INCLUDE          | false
      "http://bar.com/api" | INCLUDE          | true
      "http://foo.com/api" | EXCLUDE          | true
      "http://bar.com/api" | EXCLUDE          | true
      "http://baz.com/api" | EXCLUDE          | false
      "http://foo.com/api" | ALL              | true
      "http://bar.com/api" | ALL              | true
      "http://baz.com/api" | ALL              | true

  }

  def "should not be able to create config with invalid re"() {
    when:
      new HttpMetricsCollectorConfig(ALL, ["foo": "*foo.com.*"], [:], [:])

    then:
      thrown(PatternSyntaxException)
  }

  def "should map url"() {
    given:

      def config = new HttpMetricsCollectorConfig(ALL, ["foo": ".*foo.com.*"], [:], [:])

    when:
      def result = config.groupUrl("http://foo.com")

    then:
      result.isPresent()
      result.get() == "foo"
  }

  def "should not map url"() {
    given:

      def config = new HttpMetricsCollectorConfig(ALL, ["foo": ".*foo.com.*"], [:], [:])

    when:
      def result = config.groupUrl("http://bar.com")

    then:
      !result.isPresent()
  }
}
