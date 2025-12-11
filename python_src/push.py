from pywebpush import json, webpush, WebPushException
import sys

if __name__ == '__main__':
    print(sys.argv)
    try:
        webpush(
            subscription_info={
                "endpoint": sys.argv[1],
                "keys": {
                    "auth": sys.argv[2],
                    "p256dh": sys.argv[3]
                }
            },
            data=json.dumps({
                "body": sys.argv[4],
                "title": sys.argv[5]
            }),
            vapid_private_key=sys.argv[7],
            vapid_claims={
                "public_key": sys.argv[6],
                "sub": "mailto:"+sys.argv[8]
            }
        )
    except WebPushException as ex:
        print("Error: {}", repr(ex))
        # Mozilla returns additional information in the body of the response.
        if ex.response is not None and ex.response.json():
            extra = ex.response.json()
            print("Remote service replied with a {}:{}, {}",
            extra.code,
            extra.errno,
            extra.message)
