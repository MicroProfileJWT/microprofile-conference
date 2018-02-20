import {Injectable} from '@angular/core';
import { Http, Response } from '@angular/http';
import 'rxjs/add/operator/map';
import {Endpoint} from "./endpoint";
import {EndpointsService} from "./endpoints.service";
import {Observable} from "rxjs/Observable";
import {JwtHelper} from './JwtHelper';

export interface Credentials {
    username: string,
    password: string
}

@Injectable()
export class AuthService {
    static ACCESS_TOKEN = 'access_token';
    private endPoint: Endpoint;
    private jwtHelper: JwtHelper = new JwtHelper();

    constructor(private http: Http, private endpointsService: EndpointsService) {
        console.log("AuthService.ctor");
    }

    init(callback: () => void): void {
        console.log("AuthService.init");
        if (undefined != this.endPoint) {
            callback();
        } else {
            this.endpointsService.getEndpoint("authz")
                .then(endPoint => this.setEndpoint(endPoint))
                .then(callback)
                .catch(err => console.error("Failed to obtain authz endpoint: " + err));
            console.log("AuthService.endPoint: " + this.endPoint);
        }
    }

    login(credentials: Credentials): Observable<any> {
        console.log("Attempting to post to:" + this.endPoint);
        if (undefined == this.endPoint) {
            console.error("init must be called at least once");
            this.endpointsService.getEndpoint("authz")
                .then(endPoint => this.setEndpoint(endPoint));
            console.log("loaded endpoint: "+this.endPoint);
        }

        return this.http.post(this.endPoint.url, credentials)
            .map((response: Response) => {
                    let data = response.json();
                    if (data && data.hasOwnProperty("id_token")) {
                        console.log("Saved token to local storage, %s", data.id_token);
                        localStorage.setItem(AuthService.ACCESS_TOKEN, data.id_token);
                    }
                }
            )
    }

    loggedIn(): boolean {
        return localStorage.getItem(AuthService.ACCESS_TOKEN) != undefined && !this.isExpired();
    }

    logout(): void {
        localStorage.removeItem(AuthService.ACCESS_TOKEN);
    }

    getJwtExpiration(): Date {
        var token = localStorage.getItem(AuthService.ACCESS_TOKEN);
        var exp: Date = undefined;
        if (undefined != token) {
            exp = this.jwtHelper.getTokenExpirationDate(token);
        }
        return exp;
    }

    isExpired(): boolean {
        var token = localStorage.getItem(AuthService.ACCESS_TOKEN);
        var expired: boolean = true;
        if (undefined != token) {
            expired = this.jwtHelper.isTokenExpired(token);
        }
        return expired;
    }
    secondsToExpiration(): number {
        var token = localStorage.getItem(AuthService.ACCESS_TOKEN);
        var seconds: number = 0;
        if (undefined != token) {
            seconds = this.jwtHelper.getTokenExpirationInSeconds(token);
            if(seconds < 0) {
                seconds = 0;
            }
        }
        return seconds;
    }

    getJwtClaims(): any {
        var token = localStorage.getItem(AuthService.ACCESS_TOKEN);
        var claims: any = {};
        if (undefined != token) {
            claims = this.jwtHelper.decodeToken(token);
        }
        return claims;
    }
    getJwtClaimByName(name : string): any {
        var claims: any = this.getJwtClaims();
        var claimValue: any = undefined;
        if (claims.hasOwnProperty(name)) {
            claimValue = claims[name];
        }
        return claimValue;
    }

    setEndpoint(endPoint: Endpoint): void {
        if (undefined != endPoint) {
            console.log("setSessionEP: %s", endPoint.url);
        } else {

        }
        this.endPoint = endPoint;
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error); // TODO - Display safe error
        return Promise.reject(error.message || error);
    }
}