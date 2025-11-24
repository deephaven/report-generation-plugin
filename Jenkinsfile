@Library('dh_jenkins@develop')
import io.deephaven.jenkins.DeephavenPipeline
import io.deephaven.jenkins.settings.BuildRepo
DeephavenPipeline.builder(this, steps, 'build-reporting-plugin')
        .withEnv([ DH_CACHING: 'false' ])
        .withBuildRepo(BuildRepo.plugin)
        .withGradle('Build and Publish', 'publishAllPublicationsToCustomerRepository', false)
        .withBuildImage('dh-ci-rocky8-v12')
        .build()
