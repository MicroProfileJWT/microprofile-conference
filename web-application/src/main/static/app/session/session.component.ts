import {Component, Input, Inject, OnInit, enableProdMode} from "@angular/core";
import {Session} from "./session";
import {SessionService} from "./session.service";
import {VoteService} from "../vote/vote.service";
import {Speaker} from "../speaker/speaker";

enableProdMode();

@Component({
    selector: 'session',
    templateUrl: 'app/session/session.component.html'
})

export class SessionComponent implements OnInit {
    title = 'Conference Session';
    voted = false;
    showRawInfo: boolean = false;
    rawInfo: string = null;
    _session: Session;
    @Input() speaker: Speaker;

    constructor(@Inject(SessionService) private sessionService: SessionService,
        @Inject(VoteService) private voteService: VoteService) {
        console.log("SessionComponent.ctor");
    }

    ngOnInit(): void {
        let _self = this;
        this.voteService.init(function () {
            _self.voteService.getVotes();
            _self.querySession();
        });
    }

    get session(): Session {
        return this._session;
    }
    @Input()
    set session(session: Session) {
        console.log("new session: %s", session);
        this._session = session;
        this.rawInfo = null;
        this.querySession();
    }

    querySession(): void {
        if(this._session != null) {
            this.sessionService.retrieveRawSession(this._session.id)
                .then(info => this.rawInfo = info)
                .catch(error => console.log(error));
        }
    }

    rateSession(ratingValue: number): void {
      this.voteService.rateSession(this._session, ratingValue);
      this.voted = true;
    }
}
