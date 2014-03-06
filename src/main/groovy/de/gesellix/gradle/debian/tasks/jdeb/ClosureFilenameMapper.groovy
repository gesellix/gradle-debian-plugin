package de.gesellix.gradle.debian.tasks.jdeb;

import org.apache.commons.lang.Validate;
import org.vafer.jdeb.mapping.Mapper;
import org.vafer.jdeb.shaded.compress.compress.archivers.tar.TarArchiveEntry;

class ClosureFilenameMapper implements Mapper {

	private Closure mapping;

	def ClosureFilenameMapper(Closure mapping) {
		this.mapping = mapping;
	}

	def TarArchiveEntry map(TarArchiveEntry e) {
		if(mapping!=null)
			e.setName(mapping(e.getName()));
		return e;
	}

}
