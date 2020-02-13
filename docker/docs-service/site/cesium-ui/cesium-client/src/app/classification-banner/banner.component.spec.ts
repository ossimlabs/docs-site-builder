import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BannerComponent } from './banner.component';
import { Component } from '@angular/core';

describe('BannerComponent', () => {
  @Component({
    template: '<banner [content]="bannerContent"></banner>'
  })
  class BannerHostComponent {
    public bannerContent = {
      text: 'unclassified',
      class: 'unclassified'
    };
  }

  let component: BannerHostComponent;
  let fixture: ComponentFixture<BannerHostComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BannerComponent, BannerHostComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BannerHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display top and bottom banners', () => {
    const topBanners = fixture.nativeElement.querySelectorAll('.classification-banner');
    expect(topBanners.length).toEqual(2);
  });

  it('should display the correct text and class', () => {
    let bannerTop = fixture.nativeElement.querySelector('.classification-banner.top');
    let bannerBottom = fixture.nativeElement.querySelector('.classification-banner.bottom');

    expect(bannerTop.innerText).toEqual(component.bannerContent.text.toUpperCase());
    expect(bannerTop.classList).toContain(component.bannerContent.class);
    expect(bannerBottom.innerText).toEqual(component.bannerContent.text.toUpperCase());
    expect(bannerBottom.classList).toContain(component.bannerContent.class);

    component.bannerContent = {
      text: 'TOP SECRET//SCI',
      class: 'ts-sci'
    };

    fixture.detectChanges();
    bannerTop = fixture.nativeElement.querySelector('.classification-banner.top');
    bannerBottom = fixture.nativeElement.querySelector('.classification-banner.bottom');

    expect(bannerTop.innerText).toEqual(component.bannerContent.text.toUpperCase());
    expect(bannerTop.classList).toContain(component.bannerContent.class);
    expect(bannerBottom.innerText).toEqual(component.bannerContent.text.toUpperCase());
    expect(bannerBottom.classList).toContain(component.bannerContent.class);
  });
});
