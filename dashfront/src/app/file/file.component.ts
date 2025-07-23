import { HttpEventType, HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FileUploadService2 } from 'app/services/file-upload.service';
import { Observable } from 'rxjs';




@Component({
  selector: 'app-file',
  templateUrl: './file.component.html',
  styleUrls: ['./file.component.css']
})
export class FileComponent implements OnInit {
  

  constructor(private uploadService: FileUploadService2,) { }
  fileUris: Array<string> = [];
  ngOnInit(): void {
    this.fileInfos = this.uploadService.getFiles();
  }

  //File
currentFile?: File;
progress = 0;
message = '';

fileName = 'Select File';
fileInfos?: Observable<any>; selectedFiles?: FileList;


selectFile(event: any): void {
  if (event.target.files && event.target.files[0]) {
    const file: File = event.target.files[0];
    this.currentFile = file;
    this.fileName = this.currentFile.name;
  } else {
    this.fileName = 'Select File';
  }
}

upload(): void {
  this.progress = 0;
  this.message = "";

  if (this.currentFile) {
    this.uploadService.upload(this.currentFile).subscribe(
      (event: any) => {
        if (event.type === HttpEventType.UploadProgress) {
          this.progress = Math.round(100 * event.loaded / event.total);
        } else if (event instanceof HttpResponse) {
          this.message = event.body.message;
          this.fileInfos = this.uploadService.getFiles();
        }
      },
      (err: any) => {
        console.log(err);
        this.progress = 0;

        if (err.error && err.error.message) {
          this.message = err.error.message;
        } else {
          this.message = 'Could not upload the file!';
        }

        this.currentFile = undefined;
      });
  }



}
}
