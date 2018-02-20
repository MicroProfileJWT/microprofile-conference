import {Injectable} from "@angular/core";
import {Session} from "./session";
import {Endpoint} from "../shared/endpoint";
import {JwtService} from '../shared/jwt.service';
import "../rxjs-operators";
import {EndpointsService} from "../shared/endpoints.service";

@Injectable()
export class SessionService {

    private sessions: Session[];
    private endPoint: Endpoint;

    constructor(private jwtService: JwtService, private endpointsService: EndpointsService) {
        console.log("SessionService.ctor");
    }

    init(callback: () => void): void {

        if (undefined != this.endPoint) {
            callback();
        } else {
            this.endpointsService.getEndpoint("session")
                .then(endPoint => this.setEndpoint(endPoint))
                .then(callback)
                .catch(this.handleError);
        }
    }

    setEndpoint(endPoint: Endpoint): void {
        console.log("SessionService.setEndpoint: %s", endPoint);
        this.endPoint = endPoint;
    }

    clear(): void {
        console.log("Refreshing sessions...");
        this.sessions = undefined;
    }
    getSessions(): Promise<Session[]> {
        const hasSessions = (undefined != this.sessions);
        console.log("getSessions, hasSessions: %s, %s", hasSessions, this.sessions);
        if (hasSessions) {
            return Promise.resolve(this.sessions);
        }

        console.log("Loading sessions from: "+this.endPoint.url);
        return this.jwtService.getWithJwt(this.endPoint.url)
            .then(response => this.setSessions(response.json()))
            .catch(this.handleError);
    }

    retrieveRawSession(id: string): Promise<string> {
        var url = this.endPoint.url + "/" + id;
        console.log("Loading session info from: "+url);
        var result: Promise<string> = this.jwtService.getWithJwt(url)
            .then(response => {
                var rawSession = response.json() as string;
                return rawSession;
            })
            .catch(this.handleError);
        return result;
    }

    private setSessions(any: any): Session[] {
        console.log("setSessions called, any="+any);
        this.sessions = any as Session[];
        console.log("Loaded %d sessions", this.sessions.length);
        for(var n = 0; n < this.sessions.length; n ++) {
            console.log(this.sessions[n]);
        }
        return this.sessions;
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // TODO - Display safe error
        return Promise.reject(error.message || error);
    }

    getSessionsById(ids: string[]): Promise<Session[]> {

        if (undefined == this.endPoint) {
            console.error("init must be called at least once");
        }

        return this.getSessions().then(sessions => this.setSessions(sessions).filter(session => this.isIn(session, ids)));
    }

    private isIn(session: Session, ids: string[]): boolean {
        for (var id of ids) {
            if (session.id == id) {
                return true;
            }
        }

        return false;
    }
}
