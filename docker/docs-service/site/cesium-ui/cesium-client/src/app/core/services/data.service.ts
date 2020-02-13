import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Packet } from '../types/Packet';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from "rxjs";

export class SetVisibleEvent {
  constructor(readonly show: boolean, readonly ids: string[]) { }
}

export class PacketEvent {
  constructor(readonly session: string, readonly packets: Packet[]) { }
}

export class SelectionEvent {
  constructor(readonly id: string, readonly time: Date) { }
}

@Injectable({
  providedIn: 'root'
})
export class DataService {

  private packetSourceSource = new Subject<PacketEvent>();

  private setEntitiesVisibleSource = new Subject<SetVisibleEvent>();

  private selectionSource = new Subject<SelectionEvent>();

  private apiEndpoint = environment.API_ENDPOINT;

  constructor(private http: HttpClient) { }

  public sendPacketData(session: string, data: string) {
    this.packetSourceSource.next(new PacketEvent(session, JSON.parse(data)));
  }

  public getPacketDataObservable() {
    return this.packetSourceSource.asObservable();
  }

  public setEntitiesVisible(visibleEvent: SetVisibleEvent) {
    this.setEntitiesVisibleSource.next(visibleEvent);
  }

  public getSetEntitiesVisibleObservable() {
    return this.setEntitiesVisibleSource.asObservable();
  }

  public sendSelection(id: string, date: Date) {
    this.selectionSource.next(new SelectionEvent(id, date));
  }

  public getSelectionObservable() {
    return this.selectionSource.asObservable();
  }

  public refreshUserSession(sessionId: string): Observable<any> {
    return this.http.get(`${this.apiEndpoint}/user/session/${sessionId}/refreshCzmlCache`);
  }
}
