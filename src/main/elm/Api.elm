module Api exposing (..)

import Process
import Task
import Http
import Json.Decode as Decode
import Data.Flags exposing (Flags)
import Data.Account exposing (Account)
import Data.Version exposing (Version)
import Data.InvitePassword exposing (InvitePassword)
import Data.Invitation exposing (Invitation)
import Data.Registration exposing (Registration)
import Data.AccountExists exposing (AccountExists)
import Data.BasicStats exposing (BasicStats)
import Data.UserPass exposing (UserPass)
import Data.LoginResult exposing (LoginResult)
import Data.Track exposing (Track)
import Data.RecentTracks exposing (RecentTracks)

versionInfo: ((Result Http.Error Version) -> msg) -> Cmd msg
versionInfo receive =
    Http.get
        { url = "/api/info/version"
        , expect = Http.expectJson receive Data.Version.versionDecoder
        }

generateInvite: Flags -> InvitePassword -> ((Result Http.Error Invitation) -> msg) -> Cmd msg
generateInvite flags ip receive =
    Http.post
        { url = flags.config.apiBase ++ "/account/newinvite"
        , body = Http.jsonBody (Data.InvitePassword.encode ip)
        , expect = Http.expectJson receive Data.Invitation.decoder
        }

register: Flags -> Registration -> ((Result Http.Error ()) -> msg) -> Cmd msg
register flags reg receive =
    Http.post
        { url = flags.config.apiBase ++ "/account/register"
        , body = Http.jsonBody (Data.Registration.encode reg)
        , expect = Http.expectWhatever receive
        }

accountExists: Flags -> String -> ((Result Http.Error AccountExists) -> msg) -> Cmd msg
accountExists flags name receive =
    Http.get
        { url = flags.config.apiBase ++ "/account/exists/" ++ name
        , expect = Http.expectJson receive Data.AccountExists.decoder
        }

basicStats: Flags -> ((Result Http.Error BasicStats) -> msg) -> Cmd msg
basicStats flags receive =
    Http.get
        { url = flags.config.apiBase ++ "/stats/basic"
        , expect = Http.expectJson receive Data.BasicStats.decoder
        }

login: Flags -> UserPass -> ((Result Http.Error LoginResult) -> msg) -> Cmd msg
login flags up receive =
    Http.post
        { url = flags.config.apiBase ++ "/auth/login"
        , body = Http.jsonBody (Data.UserPass.encode up)
        , expect = Http.expectJson receive Data.LoginResult.decoder
        }

loginSession: Flags -> Account -> ((Result Http.Error LoginResult) -> msg) -> Cmd msg
loginSession flags acc receive =
    authPost
        { url = flags.config.apiBase ++ "/auth/session"
        , account = acc
        , body = Http.emptyBody
        , expect = Http.expectJson receive Data.LoginResult.decoder
        }

logout: Flags -> ((Result Http.Error ()) -> msg) -> Cmd msg
logout flags receive =
    Http.get
        { url = flags.config.apiBase ++ "/auth/logout"
        , expect = Http.expectWhatever receive
        }

getNowPlaying: ((Result Http.Error Track) -> msg) -> Flags -> Account -> Cmd msg
getNowPlaying receive flags acc =
    authGet
        { url = flags.config.apiBase ++ "/user/nowplaying"
        , account = acc
        , expect = Http.expectJson receive Data.Track.decoder
        }

getRecentTracks: ((Result Http.Error RecentTracks) -> msg) -> Flags -> Account -> Cmd msg
getRecentTracks receive flags acc =
    authGet
        { url = flags.config.apiBase ++ "/user/recenttracks"
        , account = acc
        , expect = Http.expectJson receive Data.RecentTracks.decoder
        }

getUserStats: ((Result Http.Error BasicStats) -> msg) -> Flags -> Account -> Cmd msg
getUserStats receive flags acc =
    authGet
        { url = flags.config.apiBase ++ "/user/stats"
        , account = acc
        , expect = Http.expectJson receive Data.BasicStats.decoder
        }


--- utilities

authReq: {url: String
         ,account: Account
         ,method: String
         ,headers: List Http.Header
         ,body: Http.Body
         ,expect: Http.Expect msg
         } -> Cmd msg
authReq req =
    Http.request
        { url = req.url
        , method = req.method
        , headers = (Http.header "X-Attentive-Auth-Token" req.account.authenticator) :: req.headers
        , expect = req.expect
        , body = req.body
        , timeout = Nothing
        , tracker = Nothing
        }

authPost: {url: String
          ,account: Account
          ,body: Http.Body
          ,expect: Http.Expect msg
          } -> Cmd msg
authPost req =
    authReq
        { url = req.url
        , account = req.account
        , body = req.body
        , expect = req.expect
        , method = "POST"
        , headers = []
        }

authGet: {url: String
          ,account: Account
          ,expect: Http.Expect msg
          } -> Cmd msg
authGet req =
    authReq
        { url = req.url
        , account = req.account
        , body = Http.emptyBody
        , expect = req.expect
        , method = "GET"
        , headers = []
        }
