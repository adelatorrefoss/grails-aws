package aws

import grails.test.GrailsUnitTestCase
import grails.plugin.aws.util.MockLogger
import grails.plugin.aws.s3.S3FileUpload
import org.jets3t.service.model.S3Object
import org.jets3t.service.acl.AccessControlList

class S3FileUploadTests extends GrailsUnitTestCase {
	
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testSetBucket() {
	
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
		
		s3fu.bucket("new-bucket")
		assertEquals "new-bucket", s3fu.bucket
    }

	void testSetBucketWithLocation() {
	
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
		
		s3fu.bucket("new-bucket", "location")
		assertEquals "new-bucket", s3fu.bucket
		assertEquals "location", s3fu.bucketLocation
    }

    void testSetPath() {
	
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
		
		s3fu.path("path/to/pictures")
		assertEquals "path/to/pictures", s3fu.path
    }

    void testSetMetadata() {
	
		def metaValues = [name: 'picture', type: 'user', id: '123']
		
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
				
		s3fu.metadata(metaValues)
		assertEquals metaValues, s3fu.metadata
    }

    void testSetAcl() {
	
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
		
		s3fu.acl("private")
		assertEquals "private", s3fu.acl
    }

    void testSetRrs() {
	
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
		
		s3fu.rrs(false)
		assertFalse s3fu.rrs
    }
	
	void testSetUseEncryption() {
		def s3fu = new S3FileUpload()
		s3fu.log = new MockLogger()
		
		s3fu.useEncryption(true)
		assertTrue s3fu.useEncryption
	}

	void testSetClosureData() {

		def s3fu  = new S3FileUpload()
		s3fu.log  = new MockLogger()
		
		def metaValues = [name: 'picture', type: 'user', id: '123']
		
		s3fu.setClosureData {
			bucket   "test-bucket", "EU"
		    path     "/path/to/user/files"
		    acl      "private"
		  	rrs      true
		    metadata metaValues
			useEncryption	false
		}
		
		assertEquals "test-bucket", s3fu.bucket
		assertEquals "EU", s3fu.bucketLocation
	    assertEquals "/path/to/user/files", s3fu.path
	    assertEquals "private", s3fu.acl
	    assertEquals true, s3fu.rrs
		assertEquals metaValues, s3fu.metadata
		assertEquals false, s3fu.useEncryption
	}
	
	void testValidateBucketName_Ok() {
		
		def s3fu  = new S3FileUpload()
		s3fu.log  = new MockLogger()
		
		s3fu.setClosureData {
			bucket "test-bucket"
		}
		
		s3fu.validateBucketName()
	}

	void testValidateBucketName_FailWithNoBucketName() {
		
		def s3fu  = new S3FileUpload()
		s3fu.log  = new MockLogger()
		
		shouldFail {
			s3fu.validateBucketName()
		}
	}
	
	void testBuildObjectKey_JustWithName() {
		
		def s3fu  = new S3FileUpload()
		s3fu.log  = new MockLogger()
		
		def objectKey = s3fu.buildObjectKey(null, "object-key")
		assertEquals "object-key", objectKey
	}

	void testBuildObjectKey_WithPathAndName_NoTailingSlash() {
		
		def s3fu  = new S3FileUpload()
		s3fu.log  = new MockLogger()
		
		def objectKey = s3fu.buildObjectKey("path/to/object", "object-key")
		assertEquals "path/to/object/object-key", objectKey
	}

	void testBuildObjectKey_WithPathAndName_TailingSlash() {
		
		def s3fu  = new S3FileUpload()
		s3fu.log  = new MockLogger()
		
		def objectKey = s3fu.buildObjectKey("path/to/object/", "object-key")
		assertEquals "path/to/object/object-key", objectKey
	}
	
	void testBuildS3Object() {
		
		def metaValues = [name: 'picture', type: 'user', id: '123']
		
		def s3fu      = new S3FileUpload()
		s3fu.log      = new MockLogger()
		
		s3fu.setClosureData {
			bucket   "test-bucket"
			path     "test-path"
			acl      "private"
			rrs      true
			metadata metaValues
			useEncryption	true
		}
		
		def s3Object  = s3fu.buildS3Object(new S3Object(), "key-name")
		assertEquals  AccessControlList.REST_CANNED_PRIVATE, s3Object.getAcl()
		assertEquals  S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, s3Object.getStorageClass()
		assertEquals  "test-path/key-name", s3Object.getKey()
		assertEquals  "test-bucket", s3Object.getBucketName()		
		metaValues.each { meta, value ->
			assertEquals  value, s3Object.getModifiableMetadata()[meta]
		}
		assertEquals	S3Object.SERVER_SIDE_ENCRYPTION__AES256, s3Object.getServerSideEncryptionAlgorithm()
	}
}
