package com.blackducksoftware.integration.hub.spdx.legacycode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.SpdxDocumentContainer;
import org.spdx.rdfparser.SpdxPackageVerificationCode;
import org.spdx.rdfparser.license.AnyLicenseInfo;
import org.spdx.rdfparser.license.ExtractedLicenseInfo;
import org.spdx.rdfparser.license.SpdxNoAssertionLicense;
import org.spdx.rdfparser.model.Annotation;
import org.spdx.rdfparser.model.Checksum;
import org.spdx.rdfparser.model.DoapProject;
import org.spdx.rdfparser.model.Relationship;
import org.spdx.rdfparser.model.Relationship.RelationshipType;
import org.spdx.rdfparser.model.SpdxDocument;
import org.spdx.rdfparser.model.SpdxElement;
import org.spdx.rdfparser.model.SpdxFile;
import org.spdx.rdfparser.model.SpdxFile.FileType;
import org.spdx.rdfparser.model.SpdxPackage;
import org.spdx.rdfparser.referencetype.ReferenceType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.net.MediaType;

public class SpdxLogic {

    private static final Logger logger = LoggerFactory.getLogger(SpdxLogic.class);
    private static final String SPDX_SPEC_VERSION = "SPDX-2.1";
    public static final String SPDX_URI_NAMESPACE = "http://spdx.org/rdf/terms#";
    public static final String RDFS_URI_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    public static SpdxDocument createEmptyDocument(final String uri) {

        SpdxDocumentContainer container = null;
        try {
            container = new SpdxDocumentContainer(uri, SPDX_SPEC_VERSION);
            container.getSpdxDocument().getCreationInfo().setCreators(new String[] { "Tool: SPDX Edit" });
            return container.getSpdxDocument();
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException("Unable to create blank SPDX document", e);
        }

    }

    public static void addPackageToDocument(final SpdxDocument document, final SpdxPackage pkg, final RelationshipType relType) {
        try {
            // pkg.addRelationship(new Relationship(document, RelationshipType.DESCRIBED_BY, null));
            document.addRelationship(new Relationship(pkg, relType, null));
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException("Unable to add package to document");

        }

    }

    public static Stream<SpdxPackage> getSpdxPackagesInDocument(final SpdxDocument document) {
        final Property rdfType = new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
        final String packageTypeUri = "http://spdx.org/rdf/terms#Package";
        final ResIterator resIterator = document.getDocumentContainer().getModel().listResourcesWithProperty(rdfType, document.getDocumentContainer().getModel().getResource(packageTypeUri));
        final List<SpdxPackage> result = new LinkedList<>();
        try {
            while (resIterator.hasNext()) {
                final Resource resource = resIterator.nextResource();
                result.add(new SpdxPackage(document.getDocumentContainer(), resource.asNode()));
            }
            return result.stream();
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static SpdxDocument createDocumentWithPackages(final Iterable<SpdxPackage> packages) {
        try {
            // TODO: Add document properties dialog where real URL can be
            // provided.
            final SpdxDocument document = createEmptyDocument("http://url.example.com/spdx/builder");
            for (final SpdxPackage pkg : packages) {
                final Relationship describes = new Relationship(pkg, RelationshipType.DESCRIBES, null);
                // No inverse relationship in case of multiple generations.
                document.addRelationship(describes);
            }
            return document;
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new package with the specified license, name, comment, and root path.
     *
     * @param pkgRootPath
     *            The path from which the files will be included into the package. If absent, creates a "remote" package, i.e. one without files, just referencing a remote dependency.
     * @param name
     * @param omitHiddenFiles
     * @param declaredLicense
     * @param downloadLocation
     * @return
     */
    public static SpdxPackage createSpdxPackageForPath(final Optional<Path> pkgRootPath, final SpdxDocument containingDocument, final AnyLicenseInfo declaredLicense, final String name, final String downloadLocation,
            final boolean omitHiddenFiles, final RelationshipType relType) {
        Objects.requireNonNull(pkgRootPath);
        try {

            final SpdxPackage pkg = new SpdxPackage(name, declaredLicense, new AnyLicenseInfo[] {} /* Licences from files */, null /* Declared licenses */, declaredLicense, downloadLocation, new SpdxFile[] {} /* Files */,
                    new SpdxPackageVerificationCode(null, new String[] {}));
            pkg.setLicenseInfosFromFiles(new AnyLicenseInfo[] { new SpdxNoAssertionLicense() });
            pkg.setCopyrightText("NOASSERTION");

            if (pkgRootPath.isPresent()) {
                // Add files in path

                final List<SpdxFile> addedFiles = new LinkedList<>();
                final String baseUri = pkgRootPath.get().toUri().toString();
                final FileVisitor<Path> fileVisitor = new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                        if (omitHiddenFiles && dir.getFileName().toString().startsWith(".")) {
                            return FileVisitResult.SKIP_SUBTREE;
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                        // Skip if omitHidden is set and this file is hidden.
                        if (omitHiddenFiles && (file.getFileName().toString().startsWith(".") || Files.isHidden(file))) {
                            return FileVisitResult.CONTINUE;
                        }
                        try {
                            final SpdxFile addedFile = newSpdxFile(file, baseUri);
                            addedFiles.add(addedFile);
                        } catch (final InvalidSPDXAnalysisException e) {
                            throw new RuntimeException(e);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                        logger.error("Unable to add file ", file.toAbsolutePath().toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                };
                Files.walkFileTree(pkgRootPath.get(), fileVisitor);
                final SpdxFile[] files = addedFiles.stream().toArray(size -> new SpdxFile[size]);
                pkg.setFiles(files);
                final String prefix = StringUtils.removeAll(pkgRootPath.get().getFileName().toString(), " ");
                containingDocument.getDocumentContainer().getModel().getNsPrefixMap().put(prefix, baseUri);
                recomputeVerificationCode(pkg);

            } else {
                // External package
                pkg.setFilesAnalyzed(false);
                pkg.setPackageVerificationCode(null);
            }
            addPackageToDocument(containingDocument, pkg, relType);
            return pkg;
        } catch (InvalidSPDXAnalysisException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SpdxFile addFileToPackage(final SpdxPackage pkg, final Path newFilePath, final String baseUri) {
        try {
            final SpdxFile newFile = SpdxLogic.newSpdxFile(newFilePath, baseUri);
            final SpdxFile[] newFiles = ArrayUtils.add(pkg.getFiles(), newFile);
            pkg.setFiles(newFiles);
            pkg.setFilesAnalyzed(true);
            recomputeVerificationCode(pkg);
            return newFile;
        } catch (IOException | InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static SpdxFile newSpdxFile(final Path file, final String baseUri) throws IOException, InvalidSPDXAnalysisException {
        final String checksum = getChecksumForFile(file);
        final FileType[] fileTypes = SpdxLogic.getTypesForFile(file);
        final String relativeFileUrl = StringUtils.removeStart(file.toUri().toString(), baseUri);
        return new SpdxFile(relativeFileUrl, fileTypes, checksum, new SpdxNoAssertionLicense(), new AnyLicenseInfo[] { new SpdxNoAssertionLicense() }, null, "NOASSERTION", null, null);
    }

    public static SpdxFile newSpdxFile(final Path file) throws IOException, InvalidSPDXAnalysisException {
        final String checksum = "fakeChecksum"; // getChecksumForFile(file);
        final FileType[] fileTypes = SpdxLogic.getTypesForFile(file);
        final String fileUri = file.toUri().toString();
        return new SpdxFile(fileUri, fileTypes, checksum, new SpdxNoAssertionLicense(), new AnyLicenseInfo[] { new SpdxNoAssertionLicense() }, null, "NOASSERTION", null, null);
    }

    public static SpdxFile newSpdxFile(final String filename) throws IOException, InvalidSPDXAnalysisException {
        final FileType[] fileTypes = SpdxLogic.getTypesForFilename(filename);
        // return new SpdxFile(filename, fileTypes, checksum, new SpdxNoAssertionLicense(), new AnyLicenseInfo[] { new SpdxNoAssertionLicense() }, null, "NOASSERTION", null, null);
        final String fileComment = null;
        final String copyrightText = null;
        final DoapProject[] artifactOfs = null;
        return new SpdxFile(filename, fileComment, new Annotation[0], new Relationship[0], new SpdxNoAssertionLicense(), new AnyLicenseInfo[] { new SpdxNoAssertionLicense() }, copyrightText, "NOASSERTION", fileTypes, new Checksum[0],
                new String[0], "", artifactOfs);
    }

    // TODO: Make/find a more exhaustive list
    private static final Set<String> sourceFileExtensions = ImmutableSet.of("c", "cpp", "java", "h", "cs", "cxx", "asmx", "mm", "m", "php", "groovy", "ruby", "py");
    private static final Set<String> binaryFileExtensions = ImmutableSet.of("class", "exe", "dll", "obj", "o", "jar", "bin");
    private static final Set<String> textFileExtensions = ImmutableSet.of("txt", "text");
    private static final Set<String> archiveFileExtensions = ImmutableSet.of("tar", "gz", "jar", "zip", "7z", "arj");

    // TODO: Add remaining types
    public static FileType[] getTypesForFile(final Path path) {
        final String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(path.getFileName().toString(), "."));
        final ArrayList<FileType> fileTypes = new ArrayList<>();
        addFileTypesBasedOnExtension(fileTypes, extension);
        try {
            final String mimeType = Files.probeContentType(path);
            if (StringUtils.startsWith(mimeType, MediaType.ANY_AUDIO_TYPE.type())) {
                fileTypes.add(FileType.fileType_audio);
            }
            if (StringUtils.startsWith(mimeType, MediaType.ANY_IMAGE_TYPE.type())) {
                fileTypes.add(FileType.fileType_image);
            }
            if (StringUtils.startsWith(mimeType, MediaType.ANY_APPLICATION_TYPE.type())) {
                fileTypes.add(FileType.fileType_application);
            }

        } catch (final IOException ioe) {
            logger.warn("Unable to access file " + path.toString() + " to determine its type.", ioe);
        }
        return fileTypes.toArray(new FileType[] {});
    }

    // TODO: Add remaining types
    public static FileType[] getTypesForFilename(final String filename) {
        final String extension = StringUtils.lowerCase(StringUtils.substringAfterLast(filename, "."));
        final ArrayList<FileType> fileTypes = new ArrayList<>();
        addFileTypesBasedOnExtension(fileTypes, extension);
        return fileTypes.toArray(new FileType[] {});
    }

    private static void addFileTypesBasedOnExtension(final ArrayList<FileType> fileTypes, final String extension) {
        if (sourceFileExtensions.contains(extension)) {
            fileTypes.add(SpdxFile.FileType.fileType_source);
        }
        if (binaryFileExtensions.contains(extension)) {
            fileTypes.add(FileType.fileType_binary);
        }
        if (textFileExtensions.contains(extension)) {
            fileTypes.add(FileType.fileType_text);
        }
        if (archiveFileExtensions.contains(extension)) {
            fileTypes.add(FileType.fileType_archive);
        }
        if ("spdx".equals(extension)) {
            fileTypes.add(FileType.fileType_spdx);
        }
    }

    public static String getChecksumForFile(final Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
            return DigestUtils.shaHex(is);
        }
    }

    public static String toString(final FileType fileType) {
        Objects.requireNonNull(fileType);
        return WordUtils.capitalize(StringUtils.lowerCase(fileType.getTag()));
    }

    public static String toString(final RelationshipType relationshipType) {
        Objects.requireNonNull(relationshipType);
        return WordUtils.capitalize(StringUtils.lowerCase(StringUtils.replace(relationshipType.getTag(), "_", " ")));
    }

    /**
     * Finds the first relationship that the source element has to the target of the specified type.
     *
     * @param source
     * @param relationshipType
     * @param target
     * @return
     */
    public static Optional<Relationship> findRelationship(final SpdxElement source, final RelationshipType relationshipType, final SpdxElement target) {
        Objects.requireNonNull(target);
        final List<Relationship> foundRelationships = Arrays.stream(source.getRelationships()).filter(relationship -> relationship.getRelationshipType() == relationshipType && Objects.equals(target, relationship.getRelatedSpdxElement()))
                .collect(Collectors.<Relationship> toList());
        assert (foundRelationships.size() <= 1);
        return foundRelationships.size() == 0 ? Optional.empty() : Optional.of(foundRelationships.get(0));

    }

    public static void removeRelationship(final SpdxElement source, final RelationshipType relationshipType, final SpdxElement target) {
        try {
            Objects.requireNonNull(target);
            final Relationship[] newRelationships = Arrays.stream(source.getRelationships())
                    // Filter out the relationship to remove
                    .filter(relationship -> relationship.getRelationshipType() != relationshipType && !Objects.equals(relationship.getRelatedSpdxElement(), target)).toArray(size -> new Relationship[size]);
            source.setRelationships(newRelationships);
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException("Illegal SPDX", e); // Never should
                                                           // happen
        }
    }

    /**
     * Updates whether or not a file has the specified relationship to the package.
     *
     * @param file
     * @param pkg
     * @param relationshipType
     * @param shouldExist
     *            Whether or not the file should have the specified relationship to the package.
     */
    public static void setFileRelationshipToPackage(final SpdxFile file, final SpdxPackage pkg, final RelationshipType relationshipType, final boolean shouldExist) {
        // Assuming no practical usecase requiring enforcement of atomicity
        final Optional<Relationship> existingRelationship = findRelationship(file, relationshipType, pkg);
        try {

            if (shouldExist && !existingRelationship.isPresent()) { // Create
                                                                    // the
                                                                    // relationship
                                                                    // if empty.
                final ArrayList<Relationship> newRelationships = new ArrayList<>(file.getRelationships().length + 1);
                Arrays.stream(file.getRelationships()).forEach(relationship -> newRelationships.add(relationship));
                newRelationships.add(new Relationship(pkg, relationshipType, null));
                file.setRelationships(newRelationships.toArray(new Relationship[] {}));
            }
            if (!shouldExist && existingRelationship.isPresent()) {
                final ArrayList<Relationship> newRelationships = Lists.newArrayList(file.getRelationships());
                final boolean removed = newRelationships.remove(existingRelationship);
                assert (removed);
                file.setRelationships(newRelationships.toArray(new Relationship[] {}));
            }
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeFilesFromPackage(final SpdxPackage pkg, final List<SpdxFile> filesToRemove) {
        try {
            final SpdxFile[] newFiles = Arrays.stream(pkg.getFiles()).filter(currentFile -> !filesToRemove.contains(currentFile)).toArray(size -> new SpdxFile[size]);
            pkg.setFiles(newFiles);
            if (newFiles.length == 0) {
                pkg.setFilesAnalyzed(false);
                pkg.setPackageVerificationCode(null);
            } else {
                recomputeVerificationCode(pkg);
            }
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Utility method to make verification code use in stream processing not suicide-inducing.
     *
     * @param pkg
     * @return
     */
    private static SpdxPackageVerificationCode getVerificationCodeHandlingException(final SpdxPackage pkg) {
        try {
            return pkg.getPackageVerificationCode();
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    private static Checksum getSha1Checksum(final SpdxFile file) {
        return Arrays.stream(file.getChecksums()).filter(checksum -> checksum.getAlgorithm() == Checksum.ChecksumAlgorithm.checksumAlgorithm_sha1).findFirst().get(); // Every file must have a sha
    }

    public static String computePackageVerificationCode(final SpdxPackage pkg) {
        try {
            final String combinedSha1s = Arrays.stream(pkg.getFiles()).filter(spdxFile -> !ArrayUtils.contains(getVerificationCodeHandlingException(pkg).getExcludedFileNames(), spdxFile.getName())) // Filter
                                                                                                                                                                                                      // out
                                                                                                                                                                                                      // excluded
                                                                                                                                                                                                      // files
                    .map(SpdxLogic::getSha1Checksum) // Get sha1 checksum for
                                                     // each file
                    .map(Checksum::getValue) // Get the string value of the
                                             // checksum
                    .sorted() // Sort them
                    .collect(Collectors.joining()) // Combine them into a single
                                                   // string
            ;
            assert (!"".equals(combinedSha1s));

            final String result = DigestUtils.shaHex(combinedSha1s);
            return result;

        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static void recomputeVerificationCode(final SpdxPackage pkg) {
        try {
            pkg.getPackageVerificationCode().setValue(computePackageVerificationCode(pkg));
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static void excludeFileFromVerification(final SpdxPackage pkg, final SpdxFile file) {
        try {
            if (!ArrayUtils.contains(pkg.getPackageVerificationCode().getExcludedFileNames(), file.getName())) {
                pkg.getPackageVerificationCode().addExcludedFileName(file.getName());
            }
            recomputeVerificationCode(pkg);
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unexcludeFileFromVerification(final SpdxPackage pkg, final SpdxFile file) {
        try {
            ArrayUtils.removeElement(pkg.getPackageVerificationCode().getExcludedFileNames(), file.getName());
            recomputeVerificationCode(pkg);
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isFileExcludedFromVerification(final SpdxPackage pkg, final SpdxFile file) {
        try {
            return ArrayUtils.contains(pkg.getPackageVerificationCode().getExcludedFileNames(), file.getName());
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Finds an extracted license in the document with the provided name and text.
     *
     * @param container
     * @param name
     * @param text
     * @return
     */
    public static Optional<? extends ExtractedLicenseInfo> findExtractedLicenseByNameAndText(final SpdxDocumentContainer container, final String name, final String text) {
        return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> StringUtils.equals(license.getName(), name)).filter(license -> StringUtils.equals(license.getExtractedText(), text)).findAny();

    }

    /**
     * Finds an extracted license in the document with the provided license ID
     */
    public static Optional<? extends ExtractedLicenseInfo> findExtractedLicenseInfoById(final SpdxDocumentContainer container, final String licenseId) {
        Objects.requireNonNull(licenseId);
        return Arrays.stream(container.getExtractedLicenseInfos()).filter(license -> licenseId.equals(license.getLicenseId())).findAny();
    }

    /**
     * Adds an extracted license from a file to that file and the SPDX document container. Does not verify prior existence of the license in file or document.
     *
     * @param spdxFile
     * @param documentContainer
     */
    public static void addExtractedLicenseFromFile(final SpdxFile spdxFile, final SpdxDocumentContainer documentContainer, final String licenseId, final String licenseName, final String licenseText) {
        final ExtractedLicenseInfo newLicense = new ExtractedLicenseInfo(licenseId, licenseText);
        newLicense.setName(licenseName);
        try {
            spdxFile.setLicenseInfosFromFiles(ArrayUtils.add(spdxFile.getLicenseInfoFromFiles(), newLicense));
            documentContainer.setExtractedLicenseInfos(ArrayUtils.add(documentContainer.getExtractedLicenseInfos(), newLicense));
        } catch (final InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    public static ReferenceType getReferenceType(final String string) {
        try {
            final URI uri = new URI(string);
            return new ReferenceType(uri, null, null, null);
        } catch (URISyntaxException | InvalidSPDXAnalysisException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Verifies that the provided argument is a legal SPDX document namespace.
     *
     * @return true if, and only, if the argument is a valid SPDX document namespace.
     */
    public static boolean validateDocumentNamespace(final String namespace) {
        try {
            return StringUtils.isNotBlank(namespace) && !StringUtils.contains(namespace, "#") && (new URI(namespace) != null);
        } catch (final URISyntaxException e) {
            return false;
        }
    }
}
