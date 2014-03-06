package de.gesellix.gradle.debian.tasks.jdeb

import de.gesellix.gradle.debian.tasks.data.Data
import org.gradle.api.Project
import org.vafer.jdeb.DataProducer
import org.vafer.jdeb.mapping.Mapper
import org.vafer.jdeb.mapping.PermMapper
import org.vafer.jdeb.producers.DataProducerDirectory
import org.vafer.jdeb.producers.DataProducerFile
import org.vafer.jdeb.producers.DataProducerLink

class DataProducerCreator {

  def createDataProducers(Data data, Project project) {
    def result = [] as DataProducer[]
    data.directories.each { directory ->
      assert project.file(directory.name).exists()
	  def mapper = new ClosureFilenameMapper(directory.mapper.filename);
      result = result.toList() + new DataProducerDirectory(project.file(directory.name), directory.inclusions, directory.exclusions, mapper)
    }
    data.files.each { file ->
      assert project.file(file.name).exists()
      def mapper = new PermMapper(-1, -1, null, null, file.mapper.fileMode, null, -1, null)
      result = result.toList() + new DataProducerFile(project.file(file.name), file.target, null, null, mapper)
    }
    data.links.each { link ->
      result = result.toList() + new DataProducerLink(link.path, link.name, link.symbolic, null, null, null)
    }

    return result
  }
}
