package org.ods.usecase

import java.nio.file.Files

import org.ods.parser.JUnitParser
import org.ods.service.NexusService

import spock.lang.*

import static util.FixtureHelper.*

import util.*

class SonarQubeUseCaseSpec extends SpecHelper {

    SonarQubeUseCase createUseCase(PipelineSteps steps, NexusService nexus) {
        return new SonarQubeUseCase(steps, nexus)
    }

    def "load SCRR reports from path"() {
        given:
        def tmpDir = Files.createTempDirectory("scrr-reports-")
        def tmpFile1 = Files.createTempFile(tmpDir, "scrr", ".docx") << "SCRR Report 1"
        def tmpFile2 = Files.createTempFile(tmpDir, "scrr", ".docx") << "SCRR Report 2"

        def steps = Spy(PipelineSteps)
        def nexus = Mock(NexusService)
        def usecase = createUseCase(steps, nexus)

        when:
        def result = usecase.loadSCRRReportsFromPath(tmpDir.toString())

        then:
        result.size() == 2
        result.collect { it.text }.sort() == ["SCRR Report 1", "SCRR Report 2"]

        cleanup:
        tmpDir.toFile().deleteDir()
    }

    def "load SCRR reports from path with empty path"() {
        given:
        def tmpDir = Files.createTempDirectory("scrr-reports-")

        def steps = Spy(PipelineSteps)
        def nexus = Mock(NexusService)
        def usecase = createUseCase(steps, nexus)

        when:
        def result = usecase.loadSCRRReportsFromPath(tmpDir.toString())

        then:
        result.isEmpty()

        cleanup:
        tmpDir.toFile().deleteDir()
    }

    def "upload SCRR reports to Nexus"() {
        given:
        def version = "0.1"
        def project = createProject()
        def repo = project.repositories.first()
        def type = "myType"
        def artifact = Files.createTempFile("scrr", ".docx").toFile()

        def steps = Spy(PipelineSteps)
        def nexus = Mock(NexusService)
        def usecase = createUseCase(steps, nexus)

        when:
        def result = usecase.uploadSCRRReportToNexus(version, project, repo, type, artifact)

        then:
        1 * nexus.storeArtifactFromFile(
            project.services.nexus.repository.name,
            { "${project.id.toLowerCase()}-${version}" },
            { "${type}-${repo.id}-${version}.docx" },
            artifact,
            "application/docx"
        )

        cleanup:
        artifact.delete()
    }
}
