package de.gesellix.gradle.debian.tasks.jdeb

import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.mapping.PermMapper
import org.vafer.jdeb.producers.DataProducerDirectory
import org.vafer.jdeb.producers.DataProducerFile
import org.vafer.jdeb.producers.DataProducerLink
import org.vafer.jdeb.producers.DataProducerPathTemplate

class DataProducerCreator {

  List<DataProducer> createDataProducers(Data data, Project project) {
    List<DataProducer> result = new ArrayList<>()
    data.directories.each { directory ->
      assert project.file(directory.name).exists()
      def mapper = new ClosureFilenameMapper(directory?.mapper?.filename);
      result << new DataProducerDirectory(project.file(directory.name), directory.inclusions, directory.exclusions, mapper)
    }
    data.files.each { file ->
      assert project.file(file.name).exists()
      def mapper = new PermMapper(-1, -1, null, null, file.mapper.fileMode, null, -1, null)
      result << new DataProducerFile(project.file(file.name), file.target, null, null, mapper)
    }
    data.links.each { link ->
      result << new DataProducerLink(link.path, link.name, link.symbolic, null, null, null)
    }
    data.templates.each { template ->
      result << new DataProducerPathTemplate(template.paths, null, null, null)
    }

    return result
  }

  DataProducer[] createConffileProducers(Data data, Project project) {
    def result = [] as DataProducer[]
    data.conffileDirectories.each { conffileDirectory ->
      assert project.file(conffileDirectory.name).exists()
      def mapper = new ClosureFilenameMapper(conffileDirectory?.mapper?.filename);
      result << new DataProducerDirectory(project.file(conffileDirectory.name), conffileDirectory.inclusions, conffileDirectory.exclusions, mapper)
    }
    data.conffiles.each { conffile ->
      assert project.file(conffile.name).exists()
      def mapper = new PermMapper(-1, -1, null, null, conffile.mapper.fileMode, null, -1, null)
      result << result.toList() + new DataProducerFile(project.file(conffile.name), conffile.target, null, null, mapper)
    }
    return result
  }
}
