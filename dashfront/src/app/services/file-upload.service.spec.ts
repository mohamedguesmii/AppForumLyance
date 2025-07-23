import { TestBed } from '@angular/core/testing';

import { FileUploadService2 } from './file-upload.service';

describe('FileUploadService', () => {
  let service: FileUploadService2;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FileUploadService2);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
