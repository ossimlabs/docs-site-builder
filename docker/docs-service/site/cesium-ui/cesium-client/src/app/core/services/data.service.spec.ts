import { TestBed, inject } from '@angular/core/testing';

import { DataService, PacketEvent, SetVisibleEvent, SelectionEvent } from './data.service';
import { HttpClientModule } from '@angular/common/http';

describe('DataService', () => {
  let service: DataService = null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule
      ]
    });
    service = TestBed.get(DataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send packet data', () => {
    service.getPacketDataObservable().subscribe(data => {
      expect(data).toEqual(jasmine.any(PacketEvent));
      expect(data.session).toEqual('session1234');
      expect(data.packets['key']).toEqual('value');
    });

    service.sendPacketData('session1234', '{ "key": "value" }');
  });

  it('should show events', () => {
    const event = new SetVisibleEvent(true, ['id1', 'id2']);
    service.getSetEntitiesVisibleObservable().subscribe(event => {
      expect(event).toEqual(jasmine.any(SetVisibleEvent));
      expect(event.show).toBeTruthy();
      expect(event.ids).toEqual(['id1', 'id2']);
    });

    service.setEntitiesVisible(event);
  });

  it('should send a selection', () => {
    service.getSelectionObservable().subscribe(selection => expect(selection).toEqual(new SelectionEvent('selection', new Date('1987-09-29'))));
    service.sendSelection('selection', new Date('1987-09-29'));
  });
});
