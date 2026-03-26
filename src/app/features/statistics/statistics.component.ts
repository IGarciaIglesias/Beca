import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Observable } from 'rxjs';

import { StatisticsService } from '../../core/api/statistics.service';
import { Statistics } from '../../core/api/statistics.model';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './statistics.component.html',
  styleUrls: ['./statistics.component.css']
})
export class StatisticsComponent {

  stats$!: Observable<Statistics>;

  constructor(private statisticsService: StatisticsService) {
    this.stats$ = this.statisticsService.getStatistics();
  }
}