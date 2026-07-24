import { Socket } from 'k6/x/tcp';
export default function() {
    let s = new Socket();
    console.log(Object.keys(s));
}
