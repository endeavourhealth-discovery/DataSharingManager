import { Component, OnInit } from '@angular/core';
import {Instance} from '../../models/Instance';
import {InstanceService} from '../instance.service';
import {LoggerService} from 'dds-angular8';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'app-instance-list',
  templateUrl: './instance-list.component.html',
  styleUrls: ['./instance-list.component.scss']
})
export class InstanceListComponent implements OnInit {
  loading: boolean;
  dataSource: MatTableDataSource<Instance>;
  displayedColumns: string[] = ['name', 'url'];
  selected: Instance = null;

  constructor(private instanceService: InstanceService,
              private logger: LoggerService) { }

  ngOnInit() {
    this.loadInstances();
  }

  loadInstances() {
    this.loading = true;
    this.instanceService.getInstances().subscribe(
      (instances) => {this.loading = false; this.dataSource = new MatTableDataSource(instances)},
      (error) => this.logger.error(error)
    );
  }

  selectInstance(instance: Instance) {
    this.selected = instance;
  }
}
