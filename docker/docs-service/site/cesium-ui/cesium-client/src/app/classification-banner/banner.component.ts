import { Component, OnInit, Input } from '@angular/core';

@Component({
	selector: 'banner',
	templateUrl: './banner.component.html',
	styleUrls: ['./banner.component.css']
})
export class BannerComponent implements OnInit {
	@Input('content') private content: { text: string, class: 'unclassified' | 'ts-sci' };

	constructor(
	) { }

	ngOnInit() {
	}
}